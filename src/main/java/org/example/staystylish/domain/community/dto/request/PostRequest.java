package org.example.staystylish.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostRequest(
        @NotBlank(message = "제목은 공백일 수 없습니다.") String title,
        @NotBlank(message = "내용은 공백일 수 없습니다.") String content
) {
    public static PostRequest of(String title, String content) {
        return new PostRequest(title, content);
    }
}

