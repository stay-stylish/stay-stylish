package org.example.staystylish.domain.localweather.service;

import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import reactor.core.publisher.Mono;

public interface LocalWeatherService {
    /**
     * 사용자 위경도 기준 기상 데이터 조회
     */
    Mono<UserWeatherResponse> getWeatherByLatLon(GpsRequest request);
}
