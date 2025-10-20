package org.example.staystylish.domain.product.dto.request;

/**
 * 상품 분류 요청을 위한 DTO 레코드입니다.
 * 상품명을 포함합니다.
 */
public record ProductClassificationRequest(
        String productName
) {
}
