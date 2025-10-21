package org.example.staystylish.domain.localweather.service;

import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import reactor.core.publisher.Mono;

public interface WeatherService {
    /**
     * 사용자 위경도 기준 기상 데이터 조회
     * - 반환 타입이 Mono<UserWeatherResponse>로 일치해야 합니다.
     */
    Mono<UserWeatherResponse> getWeatherByLatLon(GpsRequest request);
}
