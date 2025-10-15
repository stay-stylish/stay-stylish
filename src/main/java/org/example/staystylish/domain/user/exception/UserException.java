package org.example.staystylish.domain.user.exception;

import lombok.Getter;
import org.example.staystylish.common.consts.ErrorCode;

@Getter
public class UserException extends RuntimeException {

    private final ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
