package org.example.staystylish.domain.localweather.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import java.net.URLEncoder;
import org.example.staystylish.common.exception.advice.ExternalApiException;
import org.example.staystylish.domain.localweather.dto.WeatherItem;
import org.example.staystylish.domain.localweather.dto.WeatherResponse;
import org.example.staystylish.domain.localweather.repository.WeatherRepository;
import org.example.staystylish.domain.localweather.util.KmaGridConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OBS 초단기실황 API 호출 및 캐시 처리 서비스
 * - 사용자의 위경도를 격자로 변환 후 기상 데이터를 조회
 * - Redis 캐시에 먼저 조회 후 없으면 API 호출
 * - XML을 Map으로 파싱하여 WeatherItem 리스트 생성
 */

@Service
public class WeatherServiceImpl implements WeatherService {

    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WeatherRepository weatherRepository;
    private final String serviceKey;
    private final String baseUrl;
    private final XmlMapper xmlMapper;

    public WeatherServiceImpl(WebClient.Builder webClientBuilder,
                              RedisTemplate<String, Object> redisTemplate,
                              WeatherRepository weatherRepository,
                              @Value("${kma.serviceKey}") String serviceKey,
                              @Value("${kma.baseUrl}") String baseUrl) {
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.redisTemplate = redisTemplate;
        this.weatherRepository = weatherRepository;
        this.xmlMapper = new XmlMapper(); // XML 파서 초기화
    }

    @PostConstruct
    public void init() {
        System.out.println("KMA Service Key = " + serviceKey);
        System.out.println("KMA Base URL = " + baseUrl);

    }

    /**
     * 사용자 위경도 기준 기상 데이터 조회
     */
    @Override
    public Mono<WeatherResponse> getWeatherByLatLon(double lat, double lon) {

        if (serviceKey == null || serviceKey.isBlank()) {
            return Mono.error(new ExternalApiException("KMA service key not configured"));
        }

        // 1️⃣ 위경도 → 격자 좌표(nx, ny) 변환
        int[] xy = KmaGridConverter.latLonToGrid(lat, lon);
        int nx = xy[0];
        int ny = xy[1];

        // 2️⃣ 조회 기준 baseDate / baseTime 계산
        String[] base = getBaseDateTime();
        String baseDate = base[0];
        String baseTime = base[1];

        // 3️⃣ Redis 캐시 확인
        String cacheKey = "weather:" + nx + ":" + ny + ":" + baseDate + ":" + baseTime;
        WeatherResponse cached = (WeatherResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return Mono.just(cached);

        // 4️⃣ 인증키 URL 인코딩
        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(serviceKey, "UTF-8");
        } catch (Exception e) {
            return Mono.error(new ExternalApiException("Failed to encode service key", e));
        }

        // 5️⃣ WebClient 호출 (OBS 초단기실황 API)
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUltraSrtNcst") // OBS 전용 엔드포인트
                        .queryParam("authKey", encodedKey)
                        .queryParam("numOfRows", 100)
                        .queryParam("pageNo", 1)
                        .queryParam("dataType", "XML") // OBS는 XML 반환
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .retrieve()
                .bodyToMono(String.class) // XML 문자열로 받음
                .map(xml -> parseWeatherItemsFromXml(xml, nx, ny, baseDate, baseTime))
                .onErrorMap(WebClientRequestException.class,
                        ex -> new ExternalApiException("KMA request failed: " + ex.getMessage(), ex));
    }

    /**
     * XML → WeatherItem 리스트 변환 후 WeatherResponse 생성
     */
    private WeatherResponse parseWeatherItemsFromXml(String xml, int nx, int ny, String baseDate, String baseTime) {
        List<WeatherItem> items = new ArrayList<>();

        try {
            // XML을 Jackson 트리로 파싱
            com.fasterxml.jackson.databind.JsonNode root = xmlMapper.readTree(xml);

            com.fasterxml.jackson.databind.JsonNode header = root.path("header");
            com.fasterxml.jackson.databind.JsonNode bodyNode = root.path("body");

            // API 응답 코드 확인
            if (!header.isMissingNode()) {
                String resultCode = header.path("resultCode").asText();
                String resultMsg = header.path("resultMsg").asText();
                if (!"00".equals(resultCode)) {
                    throw new ExternalApiException("KMA API error: " + resultCode + " - " + resultMsg);
                }
            }

            // ✅ item 노드 접근 (body → items → item)
            com.fasterxml.jackson.databind.JsonNode itemsContainer = bodyNode.path("items");
            com.fasterxml.jackson.databind.JsonNode itemNodes = itemsContainer.path("item");

            // ✅ item이 배열인지 단일 객체인지 구분하여 처리
            if (itemNodes.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode itemNode : itemNodes) {
                    items.add(mapToWeatherItemNode(itemNode));
                }
            } else if (itemNodes.isObject()) {
                items.add(mapToWeatherItemNode(itemNodes));
            } else {
                System.out.println("⚠️ No 'item' nodes found in XML body: " + itemsContainer);
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

        WeatherResponse response = new WeatherResponse(items, meta);

        // 6️⃣ Redis 캐시에 저장 (baseDate + baseTime 기준)
        String cacheKey = "weather:" + nx + ":" + ny + ":" + baseDate + ":" + baseTime;
        redisTemplate.opsForValue().set(cacheKey, response);

        return response;
    }

    // Map -> WeatherItem 변환 메서드
    // 파라미터: node : Jackson이 XML을 트리 구조(JsonNode)로 파싱한 각 <item> 노드
    // XML → JsonNode → WeatherItem 과정에서 한 노드(item)를 객체로 만드는 역할
    private WeatherItem mapToWeatherItemNode(com.fasterxml.jackson.databind.JsonNode node) {
        return new WeatherItem(
                node.path("category").asText(),
                node.path("obsrValue").asText(),
                node.path("baseDate").asText(),
                node.path("baseTime").asText()
        );
    }
    /**
     * 현재 시간 기준 base_date/base_time 계산
     * OBS 초단기실황 API는 매 시각 30분에 발표
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
