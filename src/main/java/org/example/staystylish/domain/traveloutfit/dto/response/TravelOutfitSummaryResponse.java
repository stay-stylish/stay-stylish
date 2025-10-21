package org.example.staystylish.domain.traveloutfit.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.WeatherSummary;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;

public record TravelOutfitSummaryResponse(
        Long travelId,
        Long userId,
        String country,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        WeatherSummary weatherSummary,
        LocalDateTime createdAt
) {
    public static TravelOutfitSummaryResponse from(TravelOutfit outfit) {
        var weather = WeatherSummary.from(
                outfit.getAvgTemperature(),
                outfit.getAvgHumidity(),
                outfit.getRainProbability(),
                outfit.getCondition()
        );

        return new TravelOutfitSummaryResponse(
                outfit.getId(), outfit.getUserId(), outfit.getCountry(), outfit.getCity(),
                outfit.getStartDate(), outfit.getEndDate(), weather, outfit.getCreatedAt()
        );
    }
}
