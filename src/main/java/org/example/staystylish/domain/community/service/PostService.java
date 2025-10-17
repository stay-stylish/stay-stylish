package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import org.example.staystylish.domain.community.dto.request.PostRequest;
import org.example.staystylish.domain.community.dto.response.PostResponse;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.exception.CommunityErrorCode;
import org.example.staystylish.domain.community.exception.CommunityException;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    // 게시글 작성
    @Transactional
    public PostResponse createPost(User user, PostRequest request) {
        Post post = Post.builder()
                .author(user)
                .title(request.title())
                .content(request.content())
                .build();

        return PostResponse.from(postRepository.save(post));
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        return PostResponse.from(findPostById(postId));
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(User user, Long postId, PostRequest request) {
        Post post = findPostById(postId);
        validatePostOwner(user, post);

        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(User user, Long postId) {
        Post post = findPostById(postId);
        validatePostOwner(user, post);

        post.softDelete();
    }

    // 공통 유틸 메소드
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));
    }

    private void validatePostOwner(User user, Post post) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.ACCESS_DENIED);
        }
    }
}


