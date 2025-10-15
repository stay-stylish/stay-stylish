package org.example.staystylish.common.exception.advice;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.consts.CommonErrorCode;
import org.example.staystylish.common.consts.ErrorCode;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.dto.response.ApiResponse.ValidationError;
import org.example.staystylish.common.exception.GlobalException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(GlobalException e) {

        log.error("GlobalException: {}", e.getErrorCode().getMessage(), e);

        return handleExceptionInternal(e.getErrorCode());
    }

    // DTO 유효성 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {

        log.error("MethodArgumentNotValidException", e);

        return handleExceptionInternal(e, CommonErrorCode.INVALID_INPUT_VALUE);
    }

    // 요청 파라미터 타입 불일치 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {

        log.error("MethodArgumentTypeMismatchException", e);

        return handleExceptionInternal(CommonErrorCode.INVALID_TYPE_VALUE);
    }

    // 필수 요청 파라미터 누락 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {

        log.error("MissingServletRequestParameterException", e);

        return handleExceptionInternal(CommonErrorCode.MISSING_REQUEST_PARAMETER);
    }

    // HTTP 요청 본문 실패 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        log.error("HttpMessageNotReadableException", e);

        return handleExceptionInternal(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    // 그 외 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {

        log.error("UnhandledException", e);

        return handleExceptionInternal(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    // 공통 응답 처리 메서드
    private ResponseEntity<ApiResponse<?>> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.of(errorCode));
    }

    private ResponseEntity<ApiResponse<Object>> handleExceptionInternal(BindException e, ErrorCode errorCode) {
        List<ValidationError> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ApiResponse.ValidationError::of)
                .collect(Collectors.toList());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.of(errorCode, validationErrors));
    }
}
