package org.example.staystylish.domain.product.dto.response;

import java.util.List;

/**
 * 상품 분류 결과를 위한 DTO 레코드입니다.
 * 카테고리, 하위 카테고리, 스타일 태그를 포함합니다.
 */
public record ProductClassificationResponse(
        String category,
        String sub_category,
        List<String> style_tags
) {
}