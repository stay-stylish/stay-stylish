package org.example.staystylish.domain.dailyoutfit.dto.request;

import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;

public record FeedbackRequest(String categoryName, LikeStatus status) {
}