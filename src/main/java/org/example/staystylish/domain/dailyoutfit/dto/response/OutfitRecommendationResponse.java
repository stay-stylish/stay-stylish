package org.example.staystylish.domain.dailyoutfit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 의상 추천 결과를 위한 DTO 레코드입니다.
 * 추천 텍스트와 추천 카테고리 목록을 포함합니다.
 */
public record OutfitRecommendationResponse(
    @JsonProperty("recommendation_text")
    String recommendationText,

    @JsonProperty("recommended_categories")
    List<String> recommendedCategories
) {
    public static OutfitRecommendationResponse from(String recommendationText, List<String> recommendedCategories) {
        return new OutfitRecommendationResponse(recommendationText, recommendedCategories);
    }
}
