package org.example.staystylish.domain.traveloutfit.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.staystylish.common.entity.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 여행 옷차림 추천 엔티티 - 문화 제약/AI 추천은 jsonb로 저장(PostgreSQL)
 */
@Entity
@Table(name = "travel_outfit_recommendation")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TravelOutfit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "travel_outfit_id")
    private Long id;

    private Long userId;

    @Column(length = 100, nullable = false)
    private String country;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Double avgTemperature;

    private Integer avgHumidity;

    private Integer rainProbability;

    private String condition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode culturalConstraintsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode aiOutfitJson;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @Column(length = 500)
    private String errorMessage;

    // PENDING 상태의 엔티티를 생성하는 정적 팩토리 메서드
    public static TravelOutfit createPending(Long userId, String country, String city,
                                             LocalDate startDate, LocalDate endDate) {

        return TravelOutfit.builder()
                .userId(userId)
                .country(country)
                .city(city)
                .startDate(startDate)
                .endDate(endDate)
                .status(RecommendationStatus.PENDING)
                .build();
    }

    // 비동기 작업 성공 시 엔티티를 업데이트하는 메서드
    public void complete(Double avgTemp, Integer avgHumidity, Integer rainProb, String condition,
                         JsonNode culturalConstraints, JsonNode aiOutfit) {

        this.avgTemperature = avgTemp;
        this.avgHumidity = avgHumidity;
        this.rainProbability = rainProb;
        this.condition = condition;
        this.culturalConstraintsJson = culturalConstraints;
        this.aiOutfitJson = aiOutfit;
        this.status = RecommendationStatus.COMPLETED;
        this.errorMessage = null;
    }

    // 비동기 작업 실패 시 엔티티를 업데이트하는 메서드
    public void fail(String message) {

        this.status = RecommendationStatus.FAILED;
        // 오류 메시지가 너무 길 경우를 대비해 500자로 제한
        this.errorMessage = (message != null && message.length() > 500) ? message.substring(0, 500) : message;
    }
}
