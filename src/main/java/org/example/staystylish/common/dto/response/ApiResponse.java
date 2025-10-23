package org.example.staystylish.common.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.example.staystylish.common.consts.ErrorCode;
import org.example.staystylish.common.consts.SuccessCode;
import org.springframework.validation.FieldError;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답
    public static <T> ApiResponse<T> of(SuccessCode code, T data) {
        return new ApiResponse<>(
                true,
                code.getMessage(),
                data
        );
    }

    // 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> of(SuccessCode code) {
        return new ApiResponse<>(
                true,
                code.getMessage(),
                null
        );
    }

    // 실패 응답
    public static <T> ApiResponse<T> of(ErrorCode code, T data) {
        return new ApiResponse<>(
                false,
                code.getMessage(),
                data
        );
    }

    // 실패 응답 (데이터 없음)
    public static ApiResponse<?> of(ErrorCode code) {
        return new ApiResponse<>(
                false,
                code.getMessage(),
                null
        );
    }

    // DTO 유효성 검증 실패 시 상세 오류 정보 담는 클래스
    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String message;

        public static ValidationError of(final FieldError fieldError) {
            return ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
        }
    }
}