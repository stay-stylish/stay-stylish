package org.example.staystylish.domain.product.dto.response;

import java.util.List;

/**
 * 상품 분류 결과를 위한 DTO 레코드입니다.
 * 카테고리, 하위 카테고리, 스타일 태그를 포함합니다.
 */
public record ProductClassificationResponse(
        String category,
        @com.fasterxml.jackson.annotation.JsonProperty("sub_category") String subCategory,
        @com.fasterxml.jackson.annotation.JsonProperty("style_tags") List<String> styleTags
) {
}