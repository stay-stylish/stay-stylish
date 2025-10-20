package org.example.staystylish.domain.localweather.repository;

import java.util.Optional;
import org.example.staystylish.domain.localweather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByRegionOrderByForecastTimeDesc(String region);
}