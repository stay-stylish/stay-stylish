package org.example.staystylish.localweather.dto;

/**  프론트엔드에서 사용자의 gps 정보를 요청
 *
 * @param latitude
 * @param longitude
 */
public record GpsRequest(Double latitude, Double longitude) {}
