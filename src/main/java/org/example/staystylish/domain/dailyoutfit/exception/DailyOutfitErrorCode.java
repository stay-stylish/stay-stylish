package org.example.staystylish.domain.dailyoutfit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Daily-Outfit 도메인의 오류 응답 코드를 정의하는 ENUM입니다.
 */
@Getter
@RequiredArgsConstructor
public enum DailyOutfitErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "피드백할 아이템을 찾을 수 없습니다."),
    WEATHER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "날씨 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
