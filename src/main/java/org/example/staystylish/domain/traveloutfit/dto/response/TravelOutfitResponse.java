package org.example.staystylish.domain.traveloutfit.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.staystylish.domain.traveloutfit.entity.TravelOutfit;

public record TravelOutfitResponse(
        Long travelId,
        Long userId,
        String country,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        WeatherSummary weatherSummary,
        CulturalConstraints culturalConstraints,
        AiOutfit aiOutfitJson,
        List<String> safetyNotes,
        LocalDateTime createdAt
) {
    public static TravelOutfitResponse from(TravelOutfit travelOutfit,
                                            WeatherSummary summary,
                                            CulturalConstraints constraints,
                                            AiOutfit ai,
                                            List<String> notes) {
        return new TravelOutfitResponse(
                travelOutfit.getId(), travelOutfit.getUserId(), travelOutfit.getCountry(), travelOutfit.getCity(),
                travelOutfit.getStartDate(), travelOutfit.getEndDate(), summary, constraints, ai, notes,
                travelOutfit.getCreatedAt()
        );
    }

    public record WeatherSummary(
            Double avgTemperature, Integer avgHumidity,
            Integer rainProbability, String condition,
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            List<RainAdvisory> rainAdvisories,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String umbrellaSummary) {
        public static WeatherSummary from(
                Double avgTemperature,
                Integer avgHumidity,
                Integer rainProbability,
                String condition
        ) {
            return new WeatherSummary(
                    avgTemperature,
                    avgHumidity,
                    rainProbability,
                    condition,
                    List.of(),
                    null
            );
        }
    }

    public record RainAdvisory(
            LocalDate date,
            Integer rainProbability,
            String advice) {
    }

    public record CulturalConstraints(
            String notes, List<String> rules) {
    }

    public record AiOutfit(
            String summary, List<OutfitSet> outfits) {

        public record OutfitSet(
                Integer setNo, String reason, List<Item> items) {
        }

        public record Item(
                String slot, String item, String styleTag) {
        }
    }
}
