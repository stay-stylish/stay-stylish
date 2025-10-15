package org.example.staystylish.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {
    SIGNUP_SUCCESS(HttpStatus.OK, "회원가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    PROFILE_FETCH_SUCCESS(HttpStatus.OK, "내 정보 조회 성공"),
    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "회원정보 수정 성공"),
    USER_DELETE_SUCCESS(HttpStatus.OK, "회원 탈퇴 성공");

    private final HttpStatus httpStatus;
    private final String message;
}