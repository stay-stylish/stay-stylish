package org.example.staystylish.domain.globalweather.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeatherErrorCode implements ErrorCode {

    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "여행 기간은 1~14일이어야 합니다."),
    INVALID_CITY(HttpStatus.BAD_REQUEST, "유효하지 않은 도시명이거나 요청 형식 오류입니다."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "날씨 API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    EXTERNAL_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "외부 날씨 서버가 일시적으로 응답하지 않습니다."),
    PARSE_FAILED(HttpStatus.BAD_GATEWAY, "날씨 데이터를 파싱할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
