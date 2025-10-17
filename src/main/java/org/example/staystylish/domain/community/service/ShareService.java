package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.community.dto.response.ShareResponse;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.entity.Share;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.community.repository.ShareRepository;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final PostRepository postRepository;
    private final ShareRepository shareRepository;

    @Transactional
    public ShareResponse sharePost(User user, Long postId, String platform) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        shareRepository.save(Share.builder()
                .post(post)
                .user(user)
                .platform(platform)
                .build());

        post.increaseShare();
        return ShareResponse.of(post.getId(), platform, post.getShareCount());
    }
}

