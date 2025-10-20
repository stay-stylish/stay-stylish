package org.example.staystylish.domain.localweather.dto;

import java.util.Map;

/**
 * 기상청 API 요청 시 사용되는 구조를 저장하는 DTO
 * - rawJson: 원본 JSON 문자열 (API에서 받은 그대로 저장 가능)
 * - parsed: 파싱된 Map 구조 (나중에 WeatherItem으로 변환)
 *
 * Redis 또는 DB에 “원본 데이터 + 파싱 결과”를 함께 캐시할 때 유용함.
 */

public record WeatherRequest(String rawJson, Map<String, Object> parsed) {}
