package org.example.staystylish.domain.productclassification.dto.request;

/**
 * 상품 분류 요청을 위한 DTO 레코드
 * 상품명을 포함
 */
public record ProductClassificationRequest(
        String productName
) {
}
