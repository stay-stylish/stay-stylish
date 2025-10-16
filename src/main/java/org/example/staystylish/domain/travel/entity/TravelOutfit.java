package org.example.staystylish.domain.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.staystylish.common.entity.BaseEntity;

@Entity
@Table(name = "travel_outfit_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelOutfit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String country;
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;

    private Double avgTemperature;
    private Integer avgHumidity;
    private Integer rainProbability;
    private String condition;

    @Column(columnDefinition = "jsonb")
    private String culturalConstraintsJson;

    @Column(columnDefinition = "jsonb")
    private String aiOutfitJson;

    @Builder
    public static TravelOutfit create(Long userId, String country, String city,
                                      LocalDate startDate, LocalDate endDate,
                                      Double avgTemperature, Integer avgHumidity,
                                      Integer rainProbability, String condition,
                                      String culturalConstraintsJson, String aiOutfitJson) {

        return TravelOutfit.builder()
                .userId(userId)
                .country(country)
                .city(city)
                .startDate(startDate)
                .endDate(endDate)
                .avgTemperature(avgTemperature)
                .avgHumidity(avgHumidity)
                .rainProbability(rainProbability)
                .condition(condition)
                .culturalConstraintsJson(culturalConstraintsJson)
                .aiOutfitJson(aiOutfitJson)
                .build();
    }
}
