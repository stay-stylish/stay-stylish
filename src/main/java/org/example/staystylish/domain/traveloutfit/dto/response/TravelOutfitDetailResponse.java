package org.example.staystylish.domain.traveloutfit.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.staystylish.domain.traveloutfit.entity.RecommendationStatus;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;

/**
 * 여행 옷차림 추천 상세 응답 DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        LocalDateTime createdAt,
        RecommendationStatus status,
        String errorMessage) {

    public static TravelOutfitDetailResponse from(TravelOutfit outfit) {
        WeatherSummary summary = (outfit.getStatus() == RecommendationStatus.COMPLETED)
                ? new WeatherSummary(
                outfit.getAvgTemperature(),
                outfit.getAvgHumidity(),
                outfit.getRainProbability(),
                outfit.getCondition()
        )
                : null;
        return new TravelOutfitDetailResponse(
                outfit.getId(),
                outfit.getUserId(),
                outfit.getCountry(),
                outfit.getCity(),
                outfit.getStartDate(),
                outfit.getEndDate(),
                summary,
                outfit.getCulturalConstraintsJson(),
                outfit.getAiOutfitJson(),
                outfit.getCreatedAt(),
                outfit.getStatus(),
                outfit.getErrorMessage()
        );
    }

    // 상세 응답용 날씨 요약
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record WeatherSummary(
            Double avgTemperature,
            Integer avgHumidity,
            Integer rainProbability,
            String condition) {
    }
}
