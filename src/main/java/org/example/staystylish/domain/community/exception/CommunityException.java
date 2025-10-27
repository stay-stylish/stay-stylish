package org.example.staystylish.domain.community.exception;

import lombok.Getter;
import org.example.staystylish.common.code.ErrorCode;

@Getter
public class CommunityException extends RuntimeException {

    private final ErrorCode errorCode;

    public CommunityException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
