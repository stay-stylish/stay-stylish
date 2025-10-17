package org.example.staystylish.domain.community.dto.response;

public record LikeResponse(
        Long postId,
        boolean liked,
        int likeCount
) {
    public static LikeResponse of(Long postId, boolean liked, int likeCount) {
        return new LikeResponse(postId, liked, likeCount);
    }
}

