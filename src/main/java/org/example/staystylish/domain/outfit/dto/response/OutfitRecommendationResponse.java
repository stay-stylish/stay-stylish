package org.example.staystylish.domain.outfit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitRecommendationResponse {

    @JsonProperty("recommendation_text")
    private String recommendationText;

    @JsonProperty("recommended_categories")
    private List<String> recommendedCategories;
}
