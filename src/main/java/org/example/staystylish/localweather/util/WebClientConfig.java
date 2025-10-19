package org.example.staystylish.localweather.util;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0")
                .build();
    }
}