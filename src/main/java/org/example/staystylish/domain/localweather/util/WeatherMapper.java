package org.example.staystylish.domain.localweather.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.example.staystylish.domain.localweather.dto.WeatherItem;
import org.example.staystylish.domain.localweather.entity.Weather;

public class WeatherMapper {

    // WeatherItem 리스트 → Weather 엔티티 생성
    public static Weather toWeatherEntity(List<WeatherItem> items, String region) {

        Weather.WeatherBuilder builder = Weather.builder()
                .region(region)
                .forecastTime(getForecastTime(items)); // baseDate + baseTime

        for (WeatherItem item : items) {
            switch (item.category()) {
                case "T1H":
                    builder.temperature(Double.valueOf(item.obsrValue()));
                    break;
                case "REH":
                    builder.humidity(Double.valueOf(item.obsrValue()));
                    break;
                case "RN1": // 1시간 강수량
                    builder.rainfall(Double.valueOf(item.obsrValue()));
                    break;
                case "WSD": // 풍속
                    builder.windSpeed(Double.valueOf(item.obsrValue()));
                    break;
                // 필요시 PTY(강수형태) 등 추가 가능
            }
        }

        return builder.build();
    }

    // WeatherItem에서 forecastTime 계산
    private static LocalDateTime getForecastTime(List<WeatherItem> items) {
        if (items.isEmpty()) return LocalDateTime.now();
        WeatherItem first = items.get(0);
        String dateTimeStr = first.baseDate() + first.baseTime(); // YYYYMMDDHHMM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
}
