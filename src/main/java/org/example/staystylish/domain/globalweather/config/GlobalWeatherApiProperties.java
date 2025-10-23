package org.example.staystylish.domain.globalweather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weatherapi")
public record GlobalWeatherApiProperties(
        String baseUrl,
        String key
) {
}
