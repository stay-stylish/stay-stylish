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

    @Column(columnDefinition = "jsonb")
    private String culturalConstraintsJson;

    @Column(columnDefinition = "jsonb")
    private String aiOutfitJson;

    private TravelOutfit(Long userId, String country, String city,
                         LocalDate startDate, LocalDate endDate,
                         Double avgTemperature, Integer avgHumidity,
                         Integer rainProbability, String condition,
                         String culturalConstraintsJson, String aiOutfitJson) {
        this.userId = userId;
        this.country = country;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
        this.avgTemperature = avgTemperature;
        this.avgHumidity = avgHumidity;
        this.rainProbability = rainProbability;
        this.condition = condition;
        this.culturalConstraintsJson = culturalConstraintsJson;
        this.aiOutfitJson = aiOutfitJson;
    }

    public static TravelOutfit create(Long userId, String country, String city,
                                      LocalDate startDate, LocalDate endDate,
                                      Double avgTemperature, Integer avgHumidity,
                                      Integer rainProbability, String condition,
                                      String culturalConstraintsJson, String aiOutfitJson) {
        return new TravelOutfit(userId, country, city, startDate, endDate,
                avgTemperature, avgHumidity, rainProbability, condition,
                culturalConstraintsJson, aiOutfitJson);
    }
}
