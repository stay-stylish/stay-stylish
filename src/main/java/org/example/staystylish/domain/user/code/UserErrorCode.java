package org.example.staystylish.domain.user.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    USER_DELETED(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않았습니다."),
    INVALID_SESSION(HttpStatus.UNAUTHORIZED,"세션이 올바르지 않습니다."),
    EMAIL_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 이메일 인증 토큰입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    EMAIL_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일 인증 대상 사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 이메일 인증이 완료된 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}