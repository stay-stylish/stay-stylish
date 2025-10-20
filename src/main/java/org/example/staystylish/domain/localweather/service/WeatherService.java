package org.example.staystylish.domain.localweather.service;

import org.example.staystylish.domain.localweather.dto.WeatherResponse;
import reactor.core.publisher.Mono;

public interface WeatherService {

    /**
     * 위도/경도 기반 날씨 조회
     * @param lat 위도
     * @param lon 경도
     * @return WeatherResponse를 감싼 Mono
     */
    Mono<WeatherResponse> getWeatherByLatLon(double lat, double lon);
}
