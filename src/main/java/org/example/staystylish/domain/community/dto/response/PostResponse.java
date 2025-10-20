package org.example.staystylish.domain.community.dto.response;

import org.example.staystylish.domain.community.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorNickname,
        int likeCount,
        int shareCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getNickname(),
                post.getLikeCount(),
                post.getShareCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}