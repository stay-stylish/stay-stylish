package org.example.staystylish.weather.repository;

import java.util.Optional;
import org.example.staystylish.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByRegionOrderByForecastTimeDesc(String region);
}