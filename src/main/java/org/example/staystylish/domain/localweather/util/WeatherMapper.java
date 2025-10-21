package org.example.staystylish.domain.localweather.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.dto.WeatherItem;
import org.example.staystylish.domain.localweather.entity.Weather;

public class WeatherMapper {

    public static UserWeatherResponse toUserWeatherResponse(List<WeatherItem> items, Object region) {
        String province = "", city = "", district = "";
        if (region instanceof org.example.staystylish.domain.localweather.entity.Region r) {
            province = r.getProvince();
            city = r.getCity();
            district = r.getDistrict();
        }

        Double temp = null, hum = null, wind = null, rain = null;
        String sky = "맑음", pty = "없음";

        for (WeatherItem item : items) {
            switch (item.category()) {
                case "T1H": temp = Double.valueOf(item.obsrValue()); break;
                case "REH": hum = Double.valueOf(item.obsrValue()); break;
                case "RN1": rain = Double.valueOf(item.obsrValue()); break;
                case "WSD": wind = Double.valueOf(item.obsrValue()); break;
                case "PTY": pty = mapPty(item.obsrValue()); break;
                case "SKY": sky = mapSky(item.obsrValue()); break;
            }
        }

        return new UserWeatherResponse(province, city, district, temp, hum, rain, wind, sky, pty);
    }

    public static Weather toWeather(List<WeatherItem> items, String region) {
        Double temp = null, hum = null, wind = null, rain = null;
        LocalDateTime forecastTime = LocalDateTime.now();

        for (WeatherItem item : items) {
            switch (item.category()) {
                case "T1H": temp = Double.valueOf(item.obsrValue()); break;
                case "REH": hum = Double.valueOf(item.obsrValue()); break;
                case "RN1": rain = Double.valueOf(item.obsrValue()); break;
                case "WSD": wind = Double.valueOf(item.obsrValue()); break;
            }
        }

        if (!items.isEmpty()) {
            var first = items.get(0);
            forecastTime = LocalDateTime.parse(first.baseDate() + first.baseTime(),
                    DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        }

        return Weather.builder()
                .region(region)
                .temperature(temp)
                .humidity(hum)
                .rainfall(rain)
                .windSpeed(wind)
                .forecastTime(forecastTime)
                .build();
    }

    private static String mapPty(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "4" -> "소나기";
            default -> "알수없음";
        };
    }

    private static String mapSky(String code) {
        return switch (code) {
            case "1" -> "맑음";
            case "2" -> "구름조금";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> "알수없음";
        };
    }
}