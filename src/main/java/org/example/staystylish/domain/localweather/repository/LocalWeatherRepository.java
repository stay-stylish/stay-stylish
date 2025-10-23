package org.example.staystylish.domain.localweather.repository;

import java.util.Optional;
import org.example.staystylish.domain.localweather.entity.Region;
import org.example.staystylish.domain.localweather.entity.LocalWeather;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Weather 엔티티를 위한 JPA Repository - DB에서 Weather 데이터를 조회/저장/삭제/수정할 수 있음 - JpaRepository<Weather, Long> 상속: 기본 CRUD 제공
 */

public interface LocalWeatherRepository extends JpaRepository<LocalWeather, Long> {

    /**
     * 특정 지역(Region)에 대해 가장 최근에 저장된 Weather 조회 - forecastTime 기준 내림차순 정렬 후 첫 번째 항목 반환 - Optional 반환으로 데이터가 없을 경우 안전하게 처리
     * 가능
     *
     * @param region 조회 대상 Region
     * @return 해당 Region의 가장 최신 Weather, 없으면 Optional.empty()
     * <p>
     * 예시 사용: weatherRepository.findTopByRegionOrderByForecastTimeDesc(region);
     */

    Optional<LocalWeather> findTopByRegionOrderByForecastTimeDesc(Region region);
}