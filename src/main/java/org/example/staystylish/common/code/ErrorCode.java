package org.example.staystylish.common.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getHttpStatus();

    String getMessage();
}
