package org.example.staystylish.domain.outfit.dto.internal;

//프롬프트 생성 로직에 피드백 정보를 전달하기 위한 단순하고 불변적인 데이터 전달자입니다.
public record FeedbackInfo(String productName, String likeStatus) {
}
