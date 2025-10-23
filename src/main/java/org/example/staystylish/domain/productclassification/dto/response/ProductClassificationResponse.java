package org.example.staystylish.domain.productclassification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 상품 분류 결과를 위한 DTO 레코드
 * 카테고리, 하위 카테고리, 스타일 태그를 포함
 */
public record ProductClassificationResponse(
        String category,
        @JsonProperty("sub_category") String subCategory,
        @JsonProperty("style_tags") List<String> styleTags
) {
}