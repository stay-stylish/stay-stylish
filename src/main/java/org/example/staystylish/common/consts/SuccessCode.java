package org.example.staystylish.common.consts;

import org.springframework.http.HttpStatus;

public interface SuccessCode {

    HttpStatus getHttpStatus();

    String getMessage();
}
