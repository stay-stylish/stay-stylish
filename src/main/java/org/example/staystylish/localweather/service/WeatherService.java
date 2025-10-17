package org.example.staystylish.weather.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.weather.client.WeatherApiClient;
import org.example.staystylish.weather.dto.KmaResponse;
import org.example.staystylish.weather.dto.WeatherResponse;
import org.example.staystylish.weather.entity.Weather;
import org.example.staystylish.weather.repository.WeatherRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherRepository weatherRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "weather:";

    public WeatherResponse getWeather(String date, String time, int nx, int ny) {
        String key = WEATHER_KEY_PREFIX + date + ":" + time + ":" + nx + ":" + ny;

        // 1️⃣ Redis 캐시 먼저 조회
        WeatherResponse cached = (WeatherResponse) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // 2️⃣ 캐시에 없으면 기상청 API 호출
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getVilageFcst")
                        .queryParam("serviceKey", "발급받은_API_KEY")
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", date)
                        .queryParam("base_time", time)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 3️⃣ 응답 파싱
        WeatherResponse weather = parseWeatherResponse(response);

        // 4️⃣ Redis에 캐시 (예: 1시간 유효)
        redisTemplate.opsForValue().set(key, weather, Duration.ofHours(1));

        return weather;
    }

    private WeatherResponse parseWeatherResponse(String json) {
        // JSON → DTO 변환 로직 (ObjectMapper 사용)
        return new ObjectMapper().readValue(json, WeatherResponse.class);
    }
}
