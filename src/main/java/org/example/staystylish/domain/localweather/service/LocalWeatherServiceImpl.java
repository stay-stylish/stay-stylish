package org.example.staystylish.domain.localweather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.net.URLEncoder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OBS 초단기실황 API 호출 및 캐시 처리 서비스 - 사용자의 위경도를 격자로 변환 후 기상 데이터를 조회 - Redis 캐시에 먼저 조회 후 없으면 API 호출 - XML을 Map으로 파싱하여
 * WeatherItem 리스트 생성
 */

@Service
public class LocalWeatherServiceImpl implements LocalWeatherService {

    private static final Logger log = LoggerFactory.getLogger(LocalWeatherServiceImpl.class);

    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocalWeatherRepository localWeatherRepository;
    private final RegionRepository regionRepository;
    private final String serviceKey;
    private final XmlMapper xmlMapper;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final Duration CACHE_TTL = Duration.ofMinutes(35); // Redis TTL 35분


    public LocalWeatherServiceImpl(WebClient.Builder webClientBuilder,
                                   RedisTemplate<String, Object> redisTemplate,
                                   LocalWeatherRepository localWeatherRepository,
                                   RegionRepository regionRepository,
                                   @Value("${kma.serviceKey}") String serviceKey,
                                   @Value("${kma.baseUrl}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.redisTemplate = redisTemplate;
        this.localWeatherRepository = localWeatherRepository;
        this.regionRepository = regionRepository;
        this.serviceKey = serviceKey;
        this.xmlMapper = new XmlMapper();
    }

    /**
     * 사용자 위경도 기준 기상 데이터 조회
     */
    @Override
    public Mono<UserWeatherResponse> getWeatherByLatLon(GpsRequest request) {

        double lat = request.latitude();
        double lon = request.longitude();

        log.info("🌏 요청 위경도 → lat={}, lon={}", lat, lon);

        // DB에서 가장 가까운 region 조회
        Region region = regionRepository.findNearestRegions(lat, lon, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ExternalApiException("Region not found"));

        // 이 지역 정보 (region)를 최종 UserWeatherResponse에 사용
        if (serviceKey == null || serviceKey.isBlank()) {
            log.error("❌ Service Key is missing!");
            return Mono.error(new ExternalApiException("KMA service key not configured"));
        }

        // 위경도 → 격자 좌표(nx, ny) 변환
        int[] xy = KmaGridConverter.latLonToGrid(lat, lon);
        int nx = xy[0];
        int ny = xy[1];

        // 조회 기준 baseDate / baseTime 계산
        String[] base = getBaseDateTime();
        String baseDate = base[0];
        String baseTime = base[1];

        log.info("기준 시각 → baseDate={}, baseTime={}", baseDate, baseTime);

        // Redis 캐시 확인
        String cacheKey = "weather:" + nx + ":" + ny + ":" + baseDate + ":" + baseTime;
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);

        if (cachedObj != null) {

            log.info("Redis Cache HIT → key={}", cacheKey);

            // LinkedHashMap → WeatherResponse 변환
            LocalWeatherResponse cached = new com.fasterxml.jackson.databind.ObjectMapper()
                    .convertValue(cachedObj, LocalWeatherResponse.class);

            // 캐시가 있을 경우에도 최종 DTO로 변환하여 반환
            return Mono.just(LocalWeatherMapper.toUserWeatherResponse(cached.items(), region));
        }

        log.warn("Redis Cache MISS → key={}", cacheKey);


        // 인증키 URL 인코딩
        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(serviceKey, "UTF-8");
        } catch (Exception e) {
            return Mono.error(new ExternalApiException("Failed to encode service key", e));
        }

        // WebClient 호출 (OBS 초단기실황 API)
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUltraSrtNcst") // OBS 전용 엔드포인트
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
                .bodyToMono(String.class) // XML 문자열로 받음
                .map(xml -> {

                    log.info("KMA API 호출 성공");

                    LocalWeatherResponse response = parseWeatherItemsFromXml(xml, nx, ny, baseDate, baseTime);

                    List<LocalWeatherItem> items = response.items();
                    // DB 저장: WeatherMapper.toWeather 호출 시 확보된 지역명(String) 사용
                    LocalWeather localWeather = LocalWeatherMapper.toWeather(items, region);
                    localWeatherRepository.save(localWeather);
                    log.info("DB 저장 완료 (regionId={})", region.getId());

                    // Redis 캐시 저장 (TTL 적용)
                    redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
                    log.info("Redis 캐시 저장 완료 → key={}, TTL={}min", cacheKey, CACHE_TTL.toMinutes());

                    return response; // 다음 map 체인을 위해 WeatherResponse 반환
                })

                // 최종 DTO 변환: WeatherResponse를 UserWeatherResponse로 변환
                .map(localWeatherResponse -> LocalWeatherMapper.toUserWeatherResponse(localWeatherResponse.items(),
                        region))
                .onErrorMap(WebClientRequestException.class, ex -> {
                    log.error("❌ WebClient Request failed: {}", ex.getMessage(), ex);
                    return new ExternalApiException("KMA request failed: " + ex.getMessage(), ex);
                });
    }

    /**
     * XML → WeatherItem 리스트 변환 후 WeatherResponse 생성
     */
    private LocalWeatherResponse parseWeatherItemsFromXml(String xml, int nx, int ny, String baseDate,
                                                          String baseTime) {
        List<LocalWeatherItem> items = new ArrayList<>();

        try {
            // XML을 Jackson 트리로 파싱
            var root = xmlMapper.readTree(xml);

            var header = root.path("header");
            var bodyNode = root.path("body");

            // API 응답 코드 확인
            if (!header.isMissingNode()) {
                String resultCode = header.path("resultCode").asText();
                String resultMsg = header.path("resultMsg").asText();
                if (!"00".equals(resultCode)) {
                    log.error("❌ KMA API Error: {} - {}", resultCode, resultMsg);
                    throw new ExternalApiException("KMA API error: " + resultCode + " - " + resultMsg);
                }
            }

            // item 노드 접근 (items → item 순서)
            var itemsContainer = bodyNode.path("items");
            var itemNodes = itemsContainer.path("item");

            // item이 배열인지 단일 객체인지 구분하여 처리 (container 여부만 확인 후 반복)
            if (itemNodes.isContainerNode()) {
                if (itemNodes.isArray()) {
                    itemNodes.forEach(node -> items.add(mapToWeatherItemNode(node)));
                } else {
                    items.add(mapToWeatherItemNode(itemNodes));
                }
            } else {
                log.warn("⚠️ No item nodes found in XML");
            }

        } catch (Exception e) {
            log.error("❌ XML 파싱 오류", e);
        }

        Map<String, Object> meta = Map.of(
                "nx", nx, "ny", ny,
                "base_date", baseDate,
                "base_time", baseTime
        );

        return new LocalWeatherResponse(items, meta);
    }



    /**
     * Map -> WeatherItem 변환 메서드 파라미터: node : Jackson이 XML을 트리 구조(JsonNode)로 파싱한 각 <item> 노드 XML → JsonNode →
     * WeatherItem 과정에서 한 노드(item)를 객체로 만드는 역할
     */
    private LocalWeatherItem mapToWeatherItemNode(com.fasterxml.jackson.databind.JsonNode node) {
        return new LocalWeatherItem(
                node.path("category").asText(),
                node.path("obsrValue").asText(),
                node.path("baseDate").asText(),
                node.path("baseTime").asText()
        );
    }

    /**
     * 현재 시간 기준 base_date/base_time 계산 OBS 초단기실황 API는 매 시각 30분에 발표
     */
    private String[] getBaseDateTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        int hour = now.getHour();
        int minute = now.getMinute();
        LocalDate baseDate = now.toLocalDate();
        String baseTime;

        // 현재 시각이 00:00~00:29면 전날 23:30 기준
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

