package org.example.staystylish.domain.travel.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import org.example.staystylish.domain.travel.entity.TravelOutfit;

public record TravelOutfitDetailResponse(
        Long travelId,
        Long userId,
        String country,
        String city,
        String startDate,
        String endDate,
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
                outfit.getStartDate().toString(), // 현재는 문자열 규격 사용
                outfit.getEndDate().toString(),
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

    public record WeatherSummary(
            Double avgTemperature,
            Integer avgHumidity,
            Integer rainProbability,
            String condition) {
    }
}
