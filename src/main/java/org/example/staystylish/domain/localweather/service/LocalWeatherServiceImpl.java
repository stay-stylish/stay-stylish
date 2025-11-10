package org.example.staystylish.domain.localweather.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.net.URLEncoder;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.staystylish.common.exception.advice.ExternalApiException;
import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.dto.LocalWeatherItem;
import org.example.staystylish.domain.localweather.dto.LocalWeatherResponse;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.entity.LocalWeather;
import org.example.staystylish.domain.localweather.repository.RegionRepository;
import org.example.staystylish.domain.localweather.repository.LocalWeatherRepository;
import org.example.staystylish.domain.localweather.util.KmaGridConverter;
import org.example.staystylish.domain.localweather.util.LocalWeatherMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import io.micrometer.core.instrument.Timer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OBS 초단기실황 API 호출 및 캐시 처리 서비스 - 사용자의 위경도를 격자로 변환 후 기상 데이터를 조회 - Redis 캐시에 먼저 조회 후 없으면 API 호출 - XML을 Map으로 파싱하여
 * WeatherItem 리스트 생성
 * Micrometer 계측 포함
 */

@Service
public class LocalWeatherServiceImpl implements LocalWeatherService {

    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocalWeatherRepository localWeatherRepository;
    private final RegionRepository regionRepository;
    private final MeterRegistry meterRegistry;
    private final String serviceKey;
    private final XmlMapper xmlMapper;

    private final Duration CACHE_TTL = Duration.ofMinutes(35); // Redis TTL 35분

    public LocalWeatherServiceImpl(WebClient.Builder webClientBuilder,
                                   RedisTemplate<String, Object> redisTemplate,
                                   LocalWeatherRepository localWeatherRepository,
                                   RegionRepository regionRepository,
                                   MeterRegistry meterRegistry,
                                   @Value("${kma.serviceKey}") String serviceKey,
                                   @Value("${kma.baseUrl}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.redisTemplate = redisTemplate;
        this.localWeatherRepository = localWeatherRepository;
        this.regionRepository = regionRepository;
        this.meterRegistry = meterRegistry;
        this.serviceKey = serviceKey;
        this.xmlMapper = new XmlMapper();


        System.out.println("✅ MeterRegistry injected: " + (meterRegistry != null));
    }

    @Override
    public Mono<UserWeatherResponse> getWeatherByLatLon(GpsRequest request) {
        double lat = request.latitude();
        double lon = request.longitude();

        // 전체 서비스 타이머 시작
        Timer.Sample totalSample = Timer.start(meterRegistry);
        meterRegistry.counter("weather.service.requests.total").increment();

        // DB에서 가까운 지역 조회
        Region region = regionRepository.findNearestRegions(lat, lon, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ExternalApiException("Region not found"));

        if (serviceKey == null || serviceKey.isBlank()) {
            return Mono.error(new ExternalApiException("KMA service key not configured"));
        }

        int[] xy = KmaGridConverter.latLonToGrid(lat, lon);
        int nx = xy[0];
        int ny = xy[1];

        String[] base = getBaseDateTime();
        String baseDate = base[0];
        String baseTime = base[1];

        String cacheKey = "weather:" + nx + ":" + ny + ":" + baseDate + ":" + baseTime;
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);

        if (cachedObj != null) {
            totalSample.stop(meterRegistry.timer("weather.service.duration.seconds", "result", "cache"));
            LocalWeatherResponse cached = new com.fasterxml.jackson.databind.ObjectMapper()
                    .convertValue(cachedObj, LocalWeatherResponse.class);
            return Mono.just(LocalWeatherMapper.toUserWeatherResponse(cached.items(), region));
        }

        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(serviceKey, "UTF-8");
        } catch (Exception e) {
            return Mono.error(new ExternalApiException("Failed to encode service key", e));
        }

        // API 타이머 시작
        Timer.Sample apiSample = Timer.start(meterRegistry);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUltraSrtNcst")
                        .queryParam("authKey", encodedKey)
                        .queryParam("numOfRows", 100)
                        .queryParam("pageNo", 1)
                        .queryParam("dataType", "XML")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> {
                    apiSample.stop(meterRegistry.timer("weather.api.duration.seconds"));
                    meterRegistry.counter("weather.api.requests.total", "result", "success").increment();
                })
                .map(xml -> {
                    // XML 파싱 계측
                    Timer.Sample parseSample = Timer.start(meterRegistry);
                    LocalWeatherResponse response = parseWeatherItemsFromXml(xml, nx, ny, baseDate, baseTime);
                    parseSample.stop(meterRegistry.timer("weather.parse.duration.seconds"));

                    // DB 저장 계측
                    Timer.Sample dbSample = Timer.start(meterRegistry);
                    LocalWeather localWeather = LocalWeatherMapper.toWeather(response.items(), region);
                    localWeatherRepository.save(localWeather);
                    dbSample.stop(meterRegistry.timer("weather.db.save.duration.seconds"));

                    // 캐시 저장
                    redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);

                    totalSample.stop(meterRegistry.timer("weather.service.duration.seconds", "result", "success"));
                    meterRegistry.counter("weather.service.requests.success").increment();

                    return LocalWeatherMapper.toUserWeatherResponse(response.items(), region);
                })
                .onErrorResume(WebClientRequestException.class, ex -> {
                    // 실패 시 계측
                    apiSample.stop(meterRegistry.timer("weather.api.duration.seconds"));
                    meterRegistry.counter("weather.api.requests.total", "result", "fail").increment();
                    meterRegistry.counter("weather.service.requests.fail").increment();
                    totalSample.stop(meterRegistry.timer("weather.service.duration.seconds", "result", "fail"));
                    if (cachedObj != null) {
                        meterRegistry.counter("weather.cache.hit").increment();
                    } else {
                        meterRegistry.counter("weather.cache.miss").increment();
                    }
                    return Mono.error(new ExternalApiException("KMA request failed: " + ex.getMessage(), ex));
                });
    }

    /** XML → WeatherItem 리스트 변환 */
    private LocalWeatherResponse parseWeatherItemsFromXml(String xml, int nx, int ny, String baseDate, String baseTime) {
        List<LocalWeatherItem> items = new ArrayList<>();
        try {
            com.fasterxml.jackson.databind.JsonNode root = xmlMapper.readTree(xml);
            com.fasterxml.jackson.databind.JsonNode header = root.path("header");
            com.fasterxml.jackson.databind.JsonNode bodyNode = root.path("body");

            if (!header.isMissingNode()) {
                String resultCode = header.path("resultCode").asText();
                if (!"00".equals(resultCode)) {
                    throw new ExternalApiException("KMA API error: " + resultCode);
                }
            }

            com.fasterxml.jackson.databind.JsonNode itemsContainer = bodyNode.path("items");
            com.fasterxml.jackson.databind.JsonNode itemNodes = itemsContainer.path("item");

            if (itemNodes.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode itemNode : itemNodes) {
                    items.add(mapToWeatherItemNode(itemNode));
                }
            } else if (itemNodes.isObject()) {
                items.add(mapToWeatherItemNode(itemNodes));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> meta = Map.of(
                "nx", nx,
                "ny", ny,
                "base_date", baseDate,
                "base_time", baseTime
        );

        return new LocalWeatherResponse(items, meta);
    }

    private LocalWeatherItem mapToWeatherItemNode(com.fasterxml.jackson.databind.JsonNode node) {
        return new LocalWeatherItem(
                node.path("category").asText(),
                node.path("obsrValue").asText(),
                node.path("baseDate").asText(),
                node.path("baseTime").asText()
        );
    }

    private String[] getBaseDateTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        int hour = now.getHour();
        int minute = now.getMinute();
        LocalDate baseDate = now.toLocalDate();
        String baseTime;

        if (minute < 30) {
            hour -= 1;
            if (hour < 0) {
                hour = 23;
                baseDate = baseDate.minusDays(1);
            }
            baseTime = String.format("%02d30", hour);
        } else {
            baseTime = String.format("%02d30", hour);
        }

        return new String[]{baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), baseTime};
    }
}