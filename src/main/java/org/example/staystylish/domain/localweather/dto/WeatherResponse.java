package org.example.staystylish.domain.localweather.dto;

import java.util.List;
import java.util.Map;

/**
 * KMA API 응답 데이터 전체를 담는 DTO
 * record로 정의하면 불변(immutable)이며 Jackson이 자동으로 직렬화/역직렬화 가능
 */

public record WeatherResponse(
        List<WeatherItem> items,
        Map<String, Object> meta
) {
    /**
     * 에러용 정적 팩토리 메서드
     */
    public static WeatherResponse error(String message) {
        return new WeatherResponse(List.of(), Map.of("error", message));
    }
}