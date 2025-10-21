package org.example.staystylish.domain.localweather.repository;

import java.util.List;
import org.example.staystylish.domain.localweather.entity.Region;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("""
    SELECT r
    FROM Region r
    ORDER BY ((r.latitude - :lat)*(r.latitude - :lat) + (r.longitude - :lon)*(r.longitude - :lon)) ASC
""")
    List<Region> findNearestRegions(@Param("lat") Double lat,
                                    @Param("lon") Double lon,
                                    Pageable pageable);


}