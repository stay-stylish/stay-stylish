package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.community.dto.response.LikeResponse;
import org.example.staystylish.domain.community.entity.Like;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.exception.CommunityErrorCode;
import org.example.staystylish.domain.community.exception.CommunityException;
import org.example.staystylish.domain.community.repository.LikeRepository;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public LikeResponse toggleLike(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        return likeRepository.findByPostAndUser(post, user)
                .map(like -> {
                    likeRepository.delete(like);
                    post.decreaseLike();
                    return LikeResponse.of(post.getId(), false, post.getLikeCount());
                })
                .orElseGet(() -> {
                    likeRepository.save(Like.builder().post(post).user(user).build());
                    post.increaseLike();
                    return LikeResponse.of(post.getId(), true, post.getLikeCount());
                });
    }
}

