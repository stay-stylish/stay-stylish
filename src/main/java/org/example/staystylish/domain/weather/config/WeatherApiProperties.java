package org.example.staystylish.domain.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weatherapi")
public record WeatherApiProperties(
        String baseUrl,
        String key
) {
}
