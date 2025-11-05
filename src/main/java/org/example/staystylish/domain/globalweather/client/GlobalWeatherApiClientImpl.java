package org.example.staystylish.domain.globalweather.client;

import static java.time.temporal.ChronoUnit.DAYS;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.common.exception.advice.ExternalApiException;
import org.example.staystylish.domain.globalweather.config.GlobalWeatherApiProperties;
import org.example.staystylish.domain.globalweather.dto.GlobalWeatherApiResponse;
import org.example.staystylish.domain.globalweather.exception.GlobalWeatherErrorCode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalWeatherApiClientImpl implements GlobalWeatherApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10); // timeout 추가
    private final GlobalWeatherApiProperties props;
    private final WebClient weatherApiWebClient;

    @Override
    @Cacheable(value = "globalWeather", key = "#city + ':' + #start + ':' + #end")
    @Retry(name = "globalWeatherApi", fallbackMethod = "fallbackGetDailyForecast")
    @CircuitBreaker(name = "globalWeatherApi", fallbackMethod = "fallbackGetDailyForecast")
    public List<Daily> getDailyForecast(String city, LocalDate start, LocalDate end) {

        LocalDate today = LocalDate.now();

        long tripDays = DAYS.between(start, end) + 1;
        long daysToStart = Math.max(0, DAYS.between(today, start));
        long days = daysToStart + tripDays;

        if (days < 1 || days > 14) {
            log.warn("잘못된 범위입니다. Start: {}, End: {}", start, end);
            throw new GlobalException(GlobalWeatherErrorCode.INVALID_DATE_RANGE);
        }

        GlobalWeatherApiResponse response = weatherApiWebClient.get()
                .uri(b -> b.path("/forecast.json")
                        .queryParam("key", props.key())
                        .queryParam("q", city)
                        .queryParam("days", days)
                        .queryParam("aqi", "no")
                        .queryParam("alerts", "no")
                        .queryParam("lang", "ko")
                        .build())
                .retrieve()
                .onStatus(s -> s.value() == 429, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new GlobalException(GlobalWeatherErrorCode.RATE_LIMITED))))
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new GlobalException(GlobalWeatherErrorCode.INVALID_CITY))))
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.bodyToMono(String.class).flatMap(
                                body -> Mono.error(new ExternalApiException(
                                        "WeatherAPI 500번대 오류 발생"))))
                .bodyToMono(GlobalWeatherApiResponse.class)
                .timeout(TIMEOUT)
                .block();

        if (response == null || response.forecast() == null || response.forecast().forecastday() == null) {
            throw new GlobalException(GlobalWeatherErrorCode.PARSE_FAILED);
        }

        var list = new ArrayList<Daily>();
        for (var fd : response.forecast().forecastday()) {
            LocalDate d0 = LocalDate.parse(fd.date());
            if (!d0.isBefore(start) && !d0.isAfter(end)) {
                var d = fd.day();
                list.add(new Daily(
                        d0,
                        d.avgTempC(),
                        d.avgHumidity(),
                        d.dailyChanceOfRain(),
                        d.condition() != null ? d.condition().text() : null
                ));
            }
        }
        return list;
    }

    public List<Daily> fallbackGetDailyForecast(String city, LocalDate start, LocalDate end, Throwable t) {

        log.warn("[CircuitBreaker] 날씨 API 호출 차단. city={}, start={}, end={}, cause={}", city, start, end,
                t.getMessage());
        // 이 예외는 processRecommendation의 catch 블록에서 처리됩니다.
        throw new GlobalException(GlobalWeatherErrorCode.EXTERNAL_UNAVAILABLE);

    }
}


