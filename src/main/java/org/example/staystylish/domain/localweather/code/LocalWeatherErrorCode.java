package org.example.staystylish.domain.localweather.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocalWeatherErrorCode implements ErrorCode {

    INVALID_GPS_INPUT(HttpStatus.BAD_REQUEST, "위도 또는 경도 입력값이 유효하지 않습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "입력한 좌표 근처의 지역 정보를 찾을 수 없습니다."),
    KMA_API_ERROR(HttpStatus.BAD_GATEWAY, "기상청 API 응답 오류가 발생했습니다."),
    KMA_SERVICE_KEY_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "기상청 API 인증키가 설정되어 있지 않습니다."),
    KMA_REQUEST_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "기상청 API 요청에 실패했습니다."),
    WEATHER_DATA_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "기상 데이터 파싱 중 오류가 발생했습니다."),
    CACHE_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 캐시 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}