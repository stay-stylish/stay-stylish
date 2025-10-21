package org.example.staystylish.domain.localweather.repository;

import java.util.List;
import org.example.staystylish.domain.localweather.entity.Region;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Region 엔티티를 위한 JPA Repository
 * - DB에서 Region 정보를 조회/저장/삭제/수정할 수 있음
 * - JpaRepository<Region, Long> 상속
 */

public interface RegionRepository extends JpaRepository<Region, Long> {

    /**
     * 위도(latitude)와 경도(longitude)를 기준으로 가장 가까운 지역 목록 조회
     * - DB 내 Region 테이블을 스캔하여, 사용자의 좌표와의 거리 계산
     * - 거리는 유클리드 거리 제곱을 이용 (단순 근사)
     *
     * @param lat 사용자 위도
     * @param lon 사용자 경도
     * @param pageable 조회할 개수, 페이지 설정
     * @return 가장 가까운 Region 리스트
     *
     */

    @Query("""
    SELECT r
    FROM Region r
    ORDER BY ((r.latitude - :lat)*(r.latitude - :lat) + (r.longitude - :lon)*(r.longitude - :lon)) ASC
""")
    List<Region> findNearestRegions(@Param("lat") Double lat,
                                    @Param("lon") Double lon,
                                    Pageable pageable);


}