package org.example.staystylish.domain.user.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {
    SIGNUP_SUCCESS(HttpStatus.OK, "회원가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    PROFILE_FETCH_SUCCESS(HttpStatus.OK, "내 정보 조회 성공"),
    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "회원정보 수정 성공"),
    USER_DELETE_SUCCESS(HttpStatus.OK, "회원 탈퇴 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 발급 성공"),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK, "이메일 인증이 완료되었습니다."),
    EMAIL_VERIFY_RESEND_SUCCESS(HttpStatus.OK, "인증 메일을 재발송했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}