package org.example.staystylish.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public record WeatherResponse() {
    private String region;
    private double temperature;
    private double rainfall;
}

