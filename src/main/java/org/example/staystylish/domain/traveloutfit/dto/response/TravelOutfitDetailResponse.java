package org.example.staystylish.domain.traveloutfit.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;

/**
 * 여행 옷차림 추천 상세 응답 DTO.
 */
public record TravelOutfitDetailResponse(
        Long travelId,
        Long userId,
        String country,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        WeatherSummary weatherSummary,
        JsonNode culturalConstraints,
        JsonNode aiOutfitJson,
        LocalDateTime createdAt) {

    public static TravelOutfitDetailResponse from(TravelOutfit outfit) {
        return new TravelOutfitDetailResponse(
                outfit.getId(),
                outfit.getUserId(),
                outfit.getCountry(),
                outfit.getCity(),
                outfit.getStartDate(),
                outfit.getEndDate(),
                new WeatherSummary(
                        outfit.getAvgTemperature(),
                        outfit.getAvgHumidity(),
                        outfit.getRainProbability(),
                        outfit.getCondition()
                ),
                outfit.getCulturalConstraintsJson(),
                outfit.getAiOutfitJson(),
                outfit.getCreatedAt()
        );
    }

    // 상세 응답용 날씨 요약
    public record WeatherSummary(
            Double avgTemperature,
            Integer avgHumidity,
            Integer rainProbability,
            String condition) {
    }
}
