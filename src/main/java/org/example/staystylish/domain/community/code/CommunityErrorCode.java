package org.example.staystylish.domain.community.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommunityErrorCode implements ErrorCode {

    // Post 관련
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 게시글만 수정 또는 삭제할 수 있습니다."),

    // Like 관련
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 내역이 존재하지 않습니다."),

    // Share 관련
    INVALID_PLATFORM(HttpStatus.BAD_REQUEST, "유효하지 않은 공유 플랫폼입니다."),

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

