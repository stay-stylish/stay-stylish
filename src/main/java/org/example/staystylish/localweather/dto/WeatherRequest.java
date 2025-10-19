package org.example.staystylish.localweather.dto;

import java.util.Map;

public record WeatherRequest(String rawJson, Map<String, Object> parsed) {}
