package org.example.staystylish.domain.localweather.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.exception.advice.ExternalApiException;
import org.example.staystylish.domain.localweather.consts.LocalWeatherErrorCode;
import org.example.staystylish.domain.localweather.consts.LocalWeatherSuccessCode;
import org.example.staystylish.domain.localweather.dto.GpsRequest;
import org.example.staystylish.domain.localweather.dto.UserWeatherResponse;
import org.example.staystylish.domain.localweather.service.LocalWeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


/**
 * POST /api/v1/weather/weather-by-gps - 사용자 위도/경도를 받아 날씨 정보 조회 - 비동기 Mono<ResponseEntity<WeatherResponse>> 반환
 */

@CrossOrigin(origins = "*") // 개발용: 프론트엔드 다른 포트 허용
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class LocalWeatherController {

    private final LocalWeatherService localWeatherService;

    @PostMapping("/weather-by-gps")
    public Mono<ResponseEntity<ApiResponse<UserWeatherResponse>>> weatherByGps(@RequestBody GpsRequest req) {

        if (req.latitude() == null || req.longitude() == null) {
            return Mono.just(
                    ResponseEntity
                            .badRequest()
                            .body(ApiResponse.of(LocalWeatherErrorCode.INVALID_GPS_INPUT))
            );
        }

        return localWeatherService.getWeatherByLatLon(req)
                .map(response ->
                        ResponseEntity.ok(ApiResponse.of(LocalWeatherSuccessCode.GET_WEATHER_SUCCESS, response))
                )
                .onErrorResume(ex -> {
                    if (ex instanceof ExternalApiException) {
                        return Mono.just(
                                ResponseEntity
                                        .status(HttpStatus.BAD_GATEWAY)
                                        .body(ApiResponse.of(LocalWeatherErrorCode.KMA_REQUEST_FAILED))
                        );
                    }
                    return Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(ApiResponse.of(LocalWeatherErrorCode.WEATHER_DATA_PARSING_FAILED))
                    );
                });
    }
}