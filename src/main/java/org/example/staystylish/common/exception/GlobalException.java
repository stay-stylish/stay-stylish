package org.example.staystylish.common.exception;

import lombok.Getter;
import org.example.staystylish.common.consts.ErrorCode;
import org.example.staystylish.common.consts.SuccessCode;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;
    private final SuccessCode successCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.successCode = null;
    }
    public GlobalException(ErrorCode errorCode, SuccessCode successCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.successCode = successCode;
    }
}
