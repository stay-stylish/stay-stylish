package org.example.staystylish.domain.community.dto.response;

public record ShareResponse(
        Long postId,
        String platform,
        int shareCount
) {
    public static ShareResponse of(Long postId, String platform, int shareCount) {
        return new ShareResponse(postId, platform, shareCount);
    }
}
