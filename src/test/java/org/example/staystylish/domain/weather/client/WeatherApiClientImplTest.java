package org.example.staystylish.domain.weather.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
                org.example.staystylish.domain.weather.config.WeatherApiConfig.class,
                org.example.staystylish.domain.weather.client.WeatherApiClientImpl.class
        },
        properties = {
                // DB 관련 제외
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",

                "weatherapi.base-url=https://api.weatherapi.com/v1",
                "weatherapi.key="
        }
)
@ActiveProfiles("test")
class WeatherApiClientImplTest {
    @Autowired
    WeatherApiClient client;

    @Test
    void getDailyForecast() {
        var start = LocalDate.now();
        var end = start.plusDays(2);

        var list = client.getDailyForecast("Seoul", start, end);

        // 콘솔 출력
        list.block().forEach(d ->
                System.out.printf("avgTempC=%.1f°C, avgHumidity=%.1f%%, rainChance=%d%%, condition=%s%n",
                        d.avgTempC(), d.avgHumidity(), d.rainChance(), d.conditionText())
        );

        assertThat(list.block()).isNotEmpty();
    }
}

