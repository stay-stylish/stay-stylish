package org.example.staystylish.domain.productclassification.code;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

/**
 * 상품 분류 도메인의 성공 응답 코드를 정의하는 ENUM
 */
@RequiredArgsConstructor
public enum ProductClassificationSuccessCode implements SuccessCode {
    CLASSIFICATION_SUCCESS(HttpStatus.OK, "상품 분류에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
