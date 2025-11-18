package org.example.staystylish.domain.traveloutfit.dto.response;

public record WeatherAverages(
        double avgTemp,
        int avgHumidity,
        int avgRainProb,
        String condition
) {
    // 비어있는 리스트를 대비한 정적 팩토리 메서드
    public static WeatherAverages empty() {
        return new WeatherAverages(0.0, 0, 0, "알 수 없음");
    }
}
