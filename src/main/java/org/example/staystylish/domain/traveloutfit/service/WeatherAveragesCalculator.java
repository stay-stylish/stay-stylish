package org.example.staystylish.domain.traveloutfit.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.staystylish.domain.globalweather.client.GlobalWeatherApiClient.Daily;
import org.example.staystylish.domain.traveloutfit.dto.response.WeatherAverages;
import org.springframework.stereotype.Component;

/**
 * Daily 리스트로부터 날씨 통계 객체를 생성
 */
@Component
public class WeatherAveragesCalculator {

    public WeatherAverages calculate(List<Daily> dailyList) {

        if (dailyList == null || dailyList.isEmpty()) {
            return WeatherAverages.empty();
        }

        // 평균 기온 계산
        double avgTemp = dailyList.stream()
                .map(Daily::avgTempC)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 평균 습도 계산 (반올림)
        int avgHumidity = (int) Math.round(dailyList.stream()
                .map(Daily::avgHumidity)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0));

        // 평균 강수 확률 계산 (반올림)
        int avgRainProb = (int) Math.round(dailyList.stream()
                .map(Daily::rainChance)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0));

        // 가장 빈번한 날씨 상태 찾기
        String condition = dailyList.stream()
                .map(Daily::conditionText)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("알 수 없음");

        return new WeatherAverages(avgTemp, avgHumidity, avgRainProb, condition);
    }
}
