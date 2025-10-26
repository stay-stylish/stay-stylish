package org.example.staystylish.domain.dailyoutfit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.staystylish.domain.dailyoutfit.enums.ShoppingMallLink;

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

    /**
     * 정적 팩토리 메서드. 추천 텍스트와 카테고리 목록을 받아 DTO를 생성.
     * 내부적으로 카테고리를 기반으로 쇼핑몰 링크를 생성하여 포함.
     * @param recommendationText AI 추천 텍스트
     * @param recommendedCategories 추천된 의류 카테고리 목록
     * @return 쇼핑몰 링크가 포함된 완성된 DTO 객체
     */
    public static DailyOutfitRecommendationResponse from(String recommendationText, List<String> recommendedCategories) {
        List<String> links = recommendedCategories.stream()
                // 추천된 카테고리 목록을 스트림으로 시작.
                .flatMap(category -> ShoppingMallLink.getAllUrls(category).values().stream())
                // 각 카테고리별로 모든 쇼핑몰의 URL(Map의 Value들)을 가져와 하나의 스트림으로 합침
                .toList();
        // 합쳐진 모든 URL 문자열을 최종적인 List<String>으로 수집.

        return new DailyOutfitRecommendationResponse(recommendationText, recommendedCategories, links);
        // 추천 텍스트, 카테고리 목록, 링크 목록을 포함하는 최종 DTO 객체를 생성하여 반환.
    }
}