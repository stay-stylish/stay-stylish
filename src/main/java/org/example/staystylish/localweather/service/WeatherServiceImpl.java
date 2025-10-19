package org.example.staystylish.localweather.service;

import jakarta.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.example.staystylish.common.exception.advice.ExternalApiException;
import org.example.staystylish.localweather.dto.WeatherItem;
import org.example.staystylish.localweather.dto.WeatherResponse;
import org.example.staystylish.localweather.repository.WeatherRepository;
import org.example.staystylish.localweather.util.KmaGridConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WeatherRepository weatherRepository;
    private final String serviceKey;
    private final String baseUrl;



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
    }
    @PostConstruct
    public void init() {
        System.out.println("KMA Service Key = " + serviceKey);
        System.out.println("KMA Base URL = " + baseUrl);
    }
    @Override
    public Mono<WeatherResponse> getWeatherByLatLon(double lat, double lon) {

        if (serviceKey == null || serviceKey.isBlank()) {
            return Mono.error(new ExternalApiException("KMA service key not configured"));
        }

        // 위경도 → 격자
        int[] xy = KmaGridConverter.latLonToGrid(lat, lon);
        int nx = xy[0];
        int ny = xy[1];

        // base_date, base_time 계산
        String[] base = getBaseDateTime();
        String baseDate = base[0];
        String baseTime = base[1];

        // 캐시 확인
        String cacheKey = "weather:" + nx + ":" + ny + ":" + baseDate + ":" + baseTime;
        WeatherResponse cached = (WeatherResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return Mono.just(cached);

        // URL 인코딩된 키
        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return Mono.error(new ExternalApiException("Failed to encode service key", e));
        }

        // WebClient 호출
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getVilageFcst")
                        .queryParam("serviceKey", encodedKey)
                        .queryParam("numOfRows", 100)
                        .queryParam("pageNo", 1)
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)  // JSON을 Map으로 바로 받음
                .map(json -> {
                    // KMA JSON → WeatherItem 리스트 변환
                    List<WeatherItem> items = parseWeatherItems(json);

                    Map<String, Object> meta = Map.of(
                            "nx", nx,
                            "ny", ny,
                            "base_date", baseDate,
                            "base_time", baseTime
                    );

                    WeatherResponse response = new WeatherResponse(items, meta);

                    // Redis 캐시 저장
                    redisTemplate.opsForValue().set(cacheKey, response);

                    return response;
                })
                .onErrorMap(WebClientRequestException.class,
                        ex -> new ExternalApiException("KMA request failed: " + ex.getMessage(), ex));
    }

    private List<WeatherItem> parseWeatherItems(Map<?,?> json) {
        // 실제 KMA JSON 구조에 맞게 변환
        // 여기서는 단순 예시로 빈 리스트 반환
        return List.of();
    }

    private String[] getBaseDateTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        int hour = now.getHour();
        LocalDate baseDate = now.toLocalDate();
        String baseTime;

        if (hour < 2) { baseDate = baseDate.minusDays(1); baseTime = "2300"; }
        else if (hour < 5) baseTime = "0200";
        else if (hour < 8) baseTime = "0500";
        else if (hour < 11) baseTime = "0800";
        else if (hour < 14) baseTime = "1100";
        else if (hour < 17) baseTime = "1400";
        else if (hour < 20) baseTime = "1700";
        else if (hour < 23) baseTime = "2000";
        else baseTime = "2300";

        return new String[]{baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), baseTime};
    }
    // JSON Map -> WeatherItem List 변환
    private List<WeatherItem> parseItems(Map<String, Object> json) {
        // 실제 KMA JSON 구조에 맞춰 필요한 데이터만 추출
        List<WeatherItem> list = new ArrayList<>();
        try {
            Map<String,Object> response = (Map<String,Object>) json.get("response");
            Map<String,Object> body = (Map<String,Object>) response.get("body");
            Map<String,Object> items = (Map<String,Object>) body.get("items");
            List<Map<String,Object>> itemList = (List<Map<String,Object>>) items.get("item");

            for (Map<String,Object> it : itemList) {
                list.add(new WeatherItem(
                        (String) it.get("category"),
                        String.valueOf(it.get("fcstValue")),
                        (String) it.get("fcstDate"),
                        (String) it.get("fcstTime")
                ));
            }
        } catch (Exception e) {
            // 필요하면 로그
        }
        return list;
    }
}

