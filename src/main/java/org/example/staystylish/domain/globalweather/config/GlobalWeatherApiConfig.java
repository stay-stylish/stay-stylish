package org.example.staystylish.domain.globalweather.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WeatherAPI 연동을 위한 설정값을 담는 record application.yml의 weatherapi 값들을 매핑
 */
@Configuration
@EnableConfigurationProperties(GlobalWeatherApiProperties.class)
@RequiredArgsConstructor
public class GlobalWeatherApiConfig {

    private final GlobalWeatherApiProperties props;

    @Bean(name = "weatherApiWebClient")
    public WebClient weatherApiWebClient() {

        // 응답이 큰 경우 대비 버퍼 상향
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .exchangeStrategies(strategies)
                .build();
    }
}
