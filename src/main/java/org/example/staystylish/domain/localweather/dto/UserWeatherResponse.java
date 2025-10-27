package org.example.staystylish.domain.localweather.dto;

public record UserWeatherResponse(
        String province,
        String city,
        String district,
        Double temperature,
        Double humidity,
        Double rainfall,
        Double windSpeed,
        String sky,     // 맑음, 구름많음 등
        String pty      // 강수형태 (없음/비/눈)
) {
}