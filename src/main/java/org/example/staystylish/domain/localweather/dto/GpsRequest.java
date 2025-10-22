package org.example.staystylish.domain.localweather.dto;

/**
 * 프론트엔드에서 전달받는 사용자 GPS 요청 DTO
 * - 위도(latitude), 경도(longitude)를 요청.
 * - Controller → Service로 전달되어 격자 좌표(nx, ny)로 변환됨.
 */

public record GpsRequest(Double latitude, Double longitude) {}
