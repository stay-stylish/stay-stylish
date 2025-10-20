package org.example.staystylish.domain.community.post.service;

import org.example.staystylish.domain.community.dto.request.PostRequest;
import org.example.staystylish.domain.community.dto.response.PostResponse;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.exception.CommunityException;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.community.service.PostService;
import org.example.staystylish.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;

    @BeforeEach
    void 게시글_유저_세팅() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("수영")
                .build();

        post = Post.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .author(user)
                .build();
    }

    @Test
    void 게시글_작성_테스트_성공() {
        // given
        PostRequest request = new PostRequest("테스트 제목", "테스트 내용");
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        PostResponse response = postService.createPost(user, request);

        // then
        assertThat(response.title()).isEqualTo("테스트 제목");
        assertThat(response.content()).isEqualTo("테스트 내용");
    }

    @Test
    void 게시글_조회_테스트_성공() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.getPost(1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("테스트 제목");
    }

    @Test
    void 게시글_조회_실패_존재하지_않음() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(99L))
                .isInstanceOf(CommunityException.class)
                .hasMessageContaining("존재하지 않는 게시글입니다.");
    }

    @Test
    void 게시글_목록_조회_페이지() {
        // given
        PageRequest pageable = PageRequest.of(0, 5);
        given(postRepository.findAll(pageable))
                .willReturn(new PageImpl<>(List.of(post), pageable, 1));

        // when
        Page<PostResponse> page = postService.getAllPosts(pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).title()).isEqualTo("테스트 제목");
    }

    @Test
    void 게시글_수정_테스트_성공() {
        // given
        PostRequest updateReq = new PostRequest("수정 제목", "수정 내용");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.updatePost(user, 1L, updateReq);

        // then
        assertThat(response.title()).isEqualTo("수정 제목");
        assertThat(response.content()).isEqualTo("수정 내용");
    }
}
