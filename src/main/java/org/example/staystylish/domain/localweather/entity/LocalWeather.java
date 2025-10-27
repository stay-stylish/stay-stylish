package org.example.staystylish.domain.localweather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Weather 엔티티
 * <p>
 * - 기상청 API 또는 기타 외부 소스로부터 받은 날씨 데이터를 RDB에 저장하기 위한 클래스 - Redis 캐시와는 별도로, 장기 저장 및 조회/분석을 위해 DB에 저장 - DB 테이블: weather
 */

@Entity
@Table(name = "weather")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weather_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;
    private Double temperature;
    private Double humidity;
    private Double rainfall;
    private Double windSpeed;

    /**
     * 예보 기준 시각 - API의 base_date + base_time
     */

    @Column(name = "forecast_time")
    private LocalDateTime forecastTime;
}
