package org.example.staystylish.domain.localweather.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocalWeatherSuccessCode implements SuccessCode {

    GET_WEATHER_SUCCESS(HttpStatus.OK, "현재 위치 기반 날씨 조회 성공"),
    GET_WEATHER_FROM_CACHE(HttpStatus.OK, "캐시된 지역 날씨 데이터 조회 성공"),
    WEATHER_DATA_SAVED(HttpStatus.CREATED, "기상 데이터가 정상적으로 저장되었습니다."),
    REGION_DATA_FOUND(HttpStatus.OK, "가장 가까운 지역 정보 조회 성공");

    private final HttpStatus httpStatus;
    private final String message;
}