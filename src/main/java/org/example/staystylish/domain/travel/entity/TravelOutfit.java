package org.example.staystylish.domain.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
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

    public static TravelOutfit create(Long userId, String country, String city,
                                      LocalDate startDate, LocalDate endDate,
                                      Double avgTemperature, Integer avgHumidity,
                                      Integer rainProbability, String condition,
                                      String culturalConstraintsJson, String aiOutfitJson) {

        TravelOutfit outfit = new TravelOutfit();
        outfit.userId = userId;
        outfit.country = country;
        outfit.city = city;
        outfit.startDate = startDate;
        outfit.endDate = endDate;
        outfit.avgTemperature = avgTemperature;
        outfit.avgHumidity = avgHumidity;
        outfit.rainProbability = rainProbability;
        outfit.condition = condition;
        outfit.culturalConstraintsJson = culturalConstraintsJson;
        outfit.aiOutfitJson = aiOutfitJson;
        return outfit;
    }
}
