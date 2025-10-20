package org.example.staystylish.domain.community.share.service;

import org.example.staystylish.domain.community.dto.response.ShareResponse;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.community.repository.ShareRepository;
import org.example.staystylish.domain.community.service.ShareService;
import org.example.staystylish.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ShareServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ShareRepository shareRepository;

    @InjectMocks
    private ShareService shareService;

    private User user;
    private Post post;

    @BeforeEach
    void 게시글_세팅() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).nickname("수영").build();
        post = Post.builder().id(1L).title("테스트 게시글").build();
    }

    @Test
    void 게시글_공유_성공() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        ShareResponse response = shareService.sharePost(user, 1L, "Instagram");

        // then
        assertThat(response.platform()).isEqualTo("INSTAGRAM");
    }

    @Test
    void 게시글_공유_실패_플랫폼_미입력() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> shareService.sharePost(user, 1L, ""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("플랫폼");
    }
}
