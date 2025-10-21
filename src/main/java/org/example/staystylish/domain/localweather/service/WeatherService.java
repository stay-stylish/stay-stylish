package org.example.staystylish.domain.localweather.service;

import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.WeatherResponse;
import reactor.core.publisher.Mono;

public interface WeatherService {
    Mono<WeatherResponse> getWeatherByLatLon(GpsRequest request);
}