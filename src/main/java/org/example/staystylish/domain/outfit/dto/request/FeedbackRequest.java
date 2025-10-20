package org.example.staystylish.domain.outfit.dto.request;

import org.example.staystylish.domain.outfit.enums.LikeStatus;

/**
 * 아이템 피드백 요청을 위한 DTO 레코드입니다.
 * 아이템 ID와 피드백 상태를 포함합니다.
 */
public record FeedbackRequest(
        Long itemId,
        LikeStatus status
) {
}