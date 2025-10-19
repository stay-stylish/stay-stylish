package org.example.staystylish.domain.outfit.dto.internal;

/**
 * A simple, immutable data carrier for passing feedback info to the prompt construction logic.
 */
public record FeedbackInfo(String productName, String likeStatus) {
}
