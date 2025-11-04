package org.example.staystylish.domain.dailyoutfit.dto.request;

import org.example.staystylish.domain.dailyoutfit.enums.LikeStatus;

/**
 * 카테고리 피드백 요청을 위한 DTO 레코드
 *
 * @param categoryName 피드백 대상 카테고리명
 * @param status       피드백 상태 (좋아요/싫어요)
 */
public record FeedbackRequest(String categoryName, LikeStatus status) {
/**
 * 카테고리 피드백 요청을 위한 DTO 레코드
 *
 * @param categoryName 피드백 대상 카테고리명
 * @param status       피드백 상태 (좋아요/싫어요)
 */
public record FeedbackRequest(String categoryName, LikeStatus status) {
}