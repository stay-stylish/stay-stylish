package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.community.code.CommunityErrorCode;
import org.example.staystylish.domain.community.dto.response.LikeResponse;
import org.example.staystylish.domain.community.entity.Like;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.repository.LikeRepository;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final PostCounterService postCounterService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "postDetail", key = "#postId"),
            @CacheEvict(value = "postList", allEntries = true)
    })
    public LikeResponse toggleLike(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GlobalException(CommunityErrorCode.POST_NOT_FOUND));

        boolean isLiked = likeRepository.findByPostAndUser(post, user)
                .map(like -> {
                    likeRepository.delete(like);
                    postCounterService.decrLike(post.getId());
                    return false;
                })
                .orElseGet(() -> {
                    likeRepository.save(Like.builder().post(post).user(user).build());
                    postCounterService.incrLike(post.getId());
                    return true;
                });

        int currentCount = postCounterService.getLikeCount(post.getId());
        return LikeResponse.of(post.getId(), isLiked, currentCount);
    }
}

