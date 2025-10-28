package org.example.staystylish.common.exception.advice;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.code.CommonErrorCode;
import org.example.staystylish.common.code.ErrorCode;
import org.example.staystylish.common.dto.response.ApiResponse;
import org.example.staystylish.common.dto.response.ApiResponse.ValidationError;
import org.example.staystylish.common.exception.GlobalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 예외 처리 */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(GlobalException e) {
        logByStatus(e.getErrorCode().getHttpStatus(), e);
        return handleExceptionInternal(e.getErrorCode());
    }

    /** DTO 유효성 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("유효성 검증 실패: {}", e.getMessage());
        return handleExceptionInternal(e, CommonErrorCode.INVALID_INPUT_VALUE);
    }

    /** 요청 파라미터 타입 불일치 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("요청 파라미터 타입 불일치: {}", e.getMessage());
        return handleExceptionInternal(CommonErrorCode.INVALID_TYPE_VALUE);
    }

    /** 필수 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("필수 요청 파라미터 누락: {}", e.getMessage());
        return handleExceptionInternal(CommonErrorCode.MISSING_REQUEST_PARAMETER);
    }

    /** JSON 파싱 실패 등 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());
        return handleExceptionInternal(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    /** 지원하지 않는 HTTP 메서드 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("지원하지 않는 HTTP 메서드: {}", e.getMessage());
        return handleExceptionInternal(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    /** 그 외 모든 예외 (서버 내부 오류) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("서버 내부 오류 발생", e);
        return handleExceptionInternal(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    /** 공통 응답 처리 */
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

    /**
     * HTTP 상태 코드에 따라 로그 레벨 자동 분기
     * - 4xx → WARN
     * - 5xx → ERROR
     */
    private void logByStatus(HttpStatus status, Exception e) {
        if (status.is4xxClientError()) {
            log.warn("클라이언트 오류 발생: {}", e.getMessage());
        } else if (status.is5xxServerError()) {
            log.error("서버 오류 발생: {}", e.getMessage(), e);
        } else {
            log.info("예외 발생: {}", e.getMessage());
        }
    }
}
