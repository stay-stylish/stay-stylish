package org.example.staystylish.domain.dailyoutfit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OutfitErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "피드백할 아이템을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
