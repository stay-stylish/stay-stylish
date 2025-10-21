package org.example.staystylish.domain.localweather.dto;

import java.util.Map;

/**
 * 기상청 API 요청 구조를 저장하는 DTO
 * - rawJson: 원본 JSON 문자열 (API에서 받은 그대로 저장)
 * - parsed: 파싱된 Map 구조 (나중에 WeatherItem으로 변환)
 *
 */

public record WeatherRequest(String rawJson, Map<String, Object> parsed) {}
