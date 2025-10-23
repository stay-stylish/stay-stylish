package org.example.staystylish.domain.traveloutfit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.AiOutfit.OutfitSet;
import org.example.staystylish.domain.traveloutfit.dto.response.TravelOutfitResponse.CulturalConstraints;

/**
 * AI가 반환한 JSON을 객체로 만들기 위한 위한 DTO. 본 DTO는 서비스에서 파싱 후 응답/저장용 객체로 변환하는 데 사용된다.</p>
 */
public record AiTravelJsonResponse(
        @JsonProperty("summary") String summary,
        @JsonProperty("outfits") List<OutfitSet> outfits,
        @JsonProperty("culturalConstraints") CulturalConstraints culturalConstraints,
        @JsonProperty("safetyNotes") List<String> safetyNotes
) {
}
