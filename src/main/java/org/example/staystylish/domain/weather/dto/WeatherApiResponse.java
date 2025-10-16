package org.example.staystylish.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * WeatherAPI의 forecast.json 응답을 매핑하기 위한 DTO 필요한 데이터 외에는 무시하도록 설정
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponse(
        @JsonProperty("forecast") Forecast forecast
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Forecast(
            @JsonProperty("forecastday") List<ForecastDay> forecastday
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastDay(
            @JsonProperty("date") String date,
            @JsonProperty("day") Day day
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Day(
            @JsonProperty("avgtemp_c") Double avgTempC,
            @JsonProperty("avghumidity") Double avgHumidity,
            @JsonProperty("daily_chance_of_rain") Integer dailyChanceOfRain,
            @JsonProperty("condition") Condition condition
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Condition(
            @JsonProperty("text") String text
    ) {
    }
}
