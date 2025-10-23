package org.example.staystylish.domain.productclassification.exception;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.SuccessCode;
import org.springframework.http.HttpStatus;

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
