package org.example.staystylish.domain.community.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.consts.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommunitySuccessCode implements SuccessCode {

    // Post 관련
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "게시글이 성공적으로 작성되었습니다."),
    POST_GET_SUCCESS(HttpStatus.OK, "게시글 조회 성공"),
    POST_LIST_GET_SUCCESS(HttpStatus.OK, "게시글 목록 조회 성공"),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "게시글이 성공적으로 수정되었습니다."),
    POST_DELETE_SUCCESS(HttpStatus.OK, "게시글이 성공적으로 삭제되었습니다."),

    // Like 관련
    POST_LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "좋아요 상태가 변경되었습니다."),

    // Share 관련
    POST_SHARE_SUCCESS(HttpStatus.OK, "게시글이 성공적으로 공유되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

