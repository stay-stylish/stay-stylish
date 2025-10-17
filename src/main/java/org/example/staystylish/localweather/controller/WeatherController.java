package org.example.staystylish.weather.controller;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.weather.dto.WeatherResponse;
import org.example.staystylish.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam String region,
            @RequestParam int nx,
            @RequestParam int ny) {
        return ResponseEntity.ok(weatherService.getWeather(region, nx, ny));
    }
}