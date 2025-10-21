package org.example.staystylish.domain.localweather.repository;

import org.example.staystylish.domain.localweather.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("SELECT r FROM Region r " +
            "ORDER BY ((r.longitude - :lon)*(r.longitude - :lon) + (r.latitude - :lat)*(r.latitude - :lat)) ASC")
    Optional<Region> findTopByNearest(@Param("lat") double lat, @Param("lon") double lon);
}
