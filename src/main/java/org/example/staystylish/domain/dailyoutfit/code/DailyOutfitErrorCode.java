package org.example.staystylish.domain.dailyoutfit.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Daily-Outfit 도메인의 오류 응답 코드를 정의하는 ENUM
 */
@Getter
@RequiredArgsConstructor
public enum DailyOutfitErrorCode implements ErrorCode {

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "피드백할 아이템을 찾을 수 없습니다."),
    WEATHER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "날씨 정보를 찾을 수 없습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 AI 서비스를 사용할 수 없습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;
}
