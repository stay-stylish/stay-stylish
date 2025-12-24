package org.example.staystylish.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.frontend.deploy-url:https://www.stay-stylish.store}")
    private String vercelFrontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        frontendUrl,       // http://localhost:3000
                        vercelFrontendUrl  // https://www.stay-stylish.store
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders(
                        "Content-Type",
                        "Authorization",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
                .allowCredentials(true)
                .maxAge(3600);
    }
}