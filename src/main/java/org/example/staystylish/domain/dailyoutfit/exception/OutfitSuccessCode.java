package org.example.staystylish.domain.dailyoutfit.exception;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.SuccessCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OutfitSuccessCode implements SuccessCode {
    GET_OUTFIT_RECOMMENDATION_SUCCESS(HttpStatus.OK, "의상 추천 조회에 성공했습니다."),
    CREATE_FEEDBACK_SUCCESS(HttpStatus.CREATED, "피드백이 성공적으로 저장되었습니다."),
    DELETE_FEEDBACK_SUCCESS(HttpStatus.OK, "피드백이 성공적으로 삭제되었습니다.");

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
