package org.example.staystylish.domain.weather.client;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.weather.config.WeatherApiProperties;
import org.example.staystylish.domain.weather.consts.WeatherErrorCode;
import org.example.staystylish.domain.weather.dto.WeatherApiResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiClientImpl implements WeatherApiClient {

    private final WeatherApiProperties props;
    private final WebClient weatherApiWebClient;

    @Override
    public List<Daily> getDailyForecast(String city, LocalDate start, LocalDate end) {

        LocalDate today = LocalDate.now();

        long tripDays = DAYS.between(start, end) + 1;
        long daysToStart = Math.max(0, DAYS.between(today, start));
        long days = daysToStart + tripDays;

        if (days < 1 || days > 14) {
            log.warn("잘못된 범위입니다. Start: {}, End: {}", start, end);
            throw new GlobalException(WeatherErrorCode.INVALID_DATE_RANGE);
        }

        WeatherApiResponse response = weatherApiWebClient.get()
                .uri(b -> b.path("/forecast.json")
                        .queryParam("key", props.key())
                        .queryParam("q", city)
                        .queryParam("days", days)
                        .queryParam("aqi", "no")
                        .queryParam("alerts", "no")
                        .build())
                .retrieve()
                .onStatus(s -> s.value() == 429, r ->
                        r.bodyToMono(String.class).map(body -> new GlobalException(WeatherErrorCode.RATE_LIMITED)))
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class).map(body -> new GlobalException(WeatherErrorCode.INVALID_CITY)))
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.bodyToMono(String.class)
                                .map(body -> new GlobalException(WeatherErrorCode.EXTERNAL_UNAVAILABLE)))
                .bodyToMono(WeatherApiResponse.class)
                .block();

        if (response == null || response.forecast() == null || response.forecast().forecastday() == null) {
            throw new GlobalException(WeatherErrorCode.PARSE_FAILED);
        }

        var list = new ArrayList<Daily>();
        for (var fd : response.forecast().forecastday()) {
            LocalDate d0 = LocalDate.parse(fd.date());
            if (!d0.isBefore(start) && !d0.isAfter(end)) {
                var d = fd.day();
                list.add(new Daily(
                        d.avgTempC(),
                        d.avgHumidity(),
                        d.dailyChanceOfRain(),
                        d.condition() != null ? d.condition().text() : null
                ));
            }
        }
        return list;
    }
}
