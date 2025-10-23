package org.example.staystylish.domain.traveloutfit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.AiOutfit.OutfitSet;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.CulturalConstraints;

public record AiTravelJsonResponse(
        @JsonProperty("summary") String summary,
        @JsonProperty("outfits") List<OutfitSet> outfits,
        @JsonProperty("culturalConstraints") CulturalConstraints culturalConstraints,
        @JsonProperty("safetyNotes") List<String> safetyNotes
) {
}
