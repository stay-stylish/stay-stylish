package org.example.staystylish.domain.traveloutfit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TravelOutfitErrorCode implements ErrorCode {

    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "여행 기간이 유효하지 않거나 종료일이 시작일보다 빠릅니다. (최소 1일, 최대 14일)"),
    WEATHER_FETCH_FAILED(HttpStatus.BAD_REQUEST, "외부 날씨 정보를 가져오는 데 실패했습니다."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 추천이거나 권한이 없습니다."),
    AI_PARSE_FAILED(HttpStatus.BAD_REQUEST, "AI 응답 파싱에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
