package org.example.staystylish.localweather.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.localweather.dto.GpsRequest;
import org.example.staystylish.localweather.dto.WeatherResponse;
import org.example.staystylish.localweather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@CrossOrigin(origins = "*") // 개발용: 프론트엔드 다른 포트 허용
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/weather-by-gps")
    public Mono<ResponseEntity<WeatherResponse>> weatherByGps(@RequestBody GpsRequest req) {
        if (req.latitude() == null || req.longitude() == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(new WeatherResponse(List.of(), Map.of("error", "latitude and longitude required"))));
        }
        return weatherService.getWeatherByLatLon(req.latitude(), req.longitude())
                .map(ResponseEntity::ok);
    }

}