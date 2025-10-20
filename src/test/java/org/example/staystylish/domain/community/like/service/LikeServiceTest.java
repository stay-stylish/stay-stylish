package org.example.staystylish.domain.community.like.service;

import org.example.staystylish.domain.community.dto.response.LikeResponse;
import org.example.staystylish.domain.community.entity.Like;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.repository.LikeRepository;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.community.service.LikeService;
import org.example.staystylish.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

class LikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private Post post;

    @BeforeEach
    void 게시글_세팅() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).nickname("수영").build();
        post = Post.builder().id(1L).title("테스트 게시글").build();
    }

    @Test
    void 좋아요_추가_테스트_성공() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(likeRepository.findByPostAndUser(post, user)).willReturn(Optional.empty());

        // when
        LikeResponse response = likeService.toggleLike(user, 1L);

        // then
        assertThat(response.liked()).isTrue();
    }

    @Test
    void 좋아요_취소_테스트_성공() {
        // given
        Like like = Like.builder().post(post).user(user).build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(likeRepository.findByPostAndUser(post, user)).willReturn(Optional.of(like));

        // when
        LikeResponse response = likeService.toggleLike(user, 1L);

        // then
        assertThat(response.liked()).isFalse();
    }
}
