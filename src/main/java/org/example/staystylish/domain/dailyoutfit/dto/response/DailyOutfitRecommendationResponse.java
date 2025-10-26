package org.example.staystylish.domain.dailyoutfit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.staystylish.domain.dailyoutfit.code.ShoppingMallLink;

import java.util.List;

/**
 * 의상 추천 결과를 위한 DTO 레코드입니다.
 * 추천 텍스트와 추천 카테고리 목록을 포함합니다.
 */
public record DailyOutfitRecommendationResponse(
        @JsonProperty("recommendation_text")
        String recommendationText,

        @JsonProperty("recommended_categories")
        List<String> recommendedCategories,

        @JsonProperty("recommended_links")
                List<String> recommendedLinks
) {
    public static DailyOutfitRecommendationResponse from(String recommendationText, List<String> recommendedCategories) {
        List<String> links = recommendedCategories.stream()
                .flatMap(category -> ShoppingMallLink.getAllUrls(category).values().stream()) // 모든 쇼핑몰 링크 생성
                .toList();

        return new DailyOutfitRecommendationResponse(recommendationText, recommendedCategories, links);
    }
}