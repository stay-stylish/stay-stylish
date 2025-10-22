package org.example.staystylish.domain.globalweather.client;

import java.time.LocalDate;
import java.util.List;

public interface WeatherApiClient {

    // 도시 + 기간(14일) 일별 평균치 목록 조회
    List<Daily> getDailyForecast(String city, LocalDate start, LocalDate end);

    // 서비스 계층으로 전달
    record Daily(LocalDate date, Double avgTempC, Double avgHumidity, Integer rainChance, String conditionText) {
    }
}
