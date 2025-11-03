package org.example.staystylish.domain.traveloutfit.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TravelOutfitSuccessCode implements SuccessCode {
    CREATED(HttpStatus.CREATED, "여행 옷차림 추천 생성 완료 되었습니다."),

    REQUEST_ACCEPTED(HttpStatus.ACCEPTED, "여행 옷차림 추천 요청이 접수되었습니다. 잠시 후 상세 조회를 통해 확인해주세요."),
    
    GET_RECOMMENDATIONS_SUCCESS(HttpStatus.OK, "여행 옷차림 추천 목록 조회 성공"),
    GET_RECOMMENDATION_DETAIL_SUCCESS(HttpStatus.OK, "여행 옷차림 추천 상세 조회 성공");

    private final HttpStatus httpStatus;
    private final String message;
}
