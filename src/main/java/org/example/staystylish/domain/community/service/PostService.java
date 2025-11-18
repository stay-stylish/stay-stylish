package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.exception.GlobalException;
import org.example.staystylish.domain.community.code.CommunityErrorCode;
import org.example.staystylish.domain.community.dto.request.PostRequest;
import org.example.staystylish.domain.community.dto.response.PostResponse;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.example.staystylish.domain.user.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostCounterService postCounterService;

    // 게시글 작성
    @Transactional
    @CacheEvict(value = "postList", allEntries = true)
    public PostResponse createPost(User user, PostRequest request) {
        Post post = Post.builder()
                .author(user)
                .title(request.title())
                .content(request.content())
                .build();

        Post savedPost = postRepository.save(post);

        postCounterService.initializeCounts(savedPost.getId(), 0, 0);

        return PostResponse.from(savedPost, 0, 0);
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = findPostById(postId);

        int likeCount = postCounterService.getLikeCount(postId);
        int shareCount = postCounterService.getShareCount(postId);

        return PostResponse.from(post, likeCount, shareCount);
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable, String sortBy) {
        if ("like".equalsIgnoreCase(sortBy)) {
            List<Long> sortedPostIds = postCounterService.getPostIdsSortedByLike(
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );

            if (sortedPostIds.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Post> posts = postRepository.findAllById(sortedPostIds);

            Map<Long, Post> postMap = posts.stream()
                    .collect(Collectors.toMap(Post::getId, post -> post));

            List<Post> orderedPosts = sortedPostIds.stream()
                    .map(postMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            List<PostResponse> responses = orderedPosts.stream()
                    .map(post -> {
                        int likeCount = postCounterService.getLikeCount(post.getId());
                        int shareCount = postCounterService.getShareCount(post.getId());

                        return PostResponse.from(post, likeCount, shareCount);
                    })
                    .toList();

            long totalElements = postCounterService.getTotalPostCount();

            return new PageImpl<>(responses, pageable, totalElements);

        } else {
            Page<Post> posts = postRepository.findAllOrderByLatest(pageable);

            Page<PostResponse> result = posts.map(post -> {
                int likeCount = postCounterService.getLikeCount(post.getId());
                int shareCount = postCounterService.getShareCount(post.getId());

                return PostResponse.from(post, likeCount, shareCount);
            });

            return result;
        }
    }

    // 게시글 수정
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "postDetail", key = "#postId"),
            @CacheEvict(value = "postList", allEntries = true)
    })
    public PostResponse updatePost(User user, Long postId, PostRequest request) {
        Post post = findPostById(postId);
        validatePostOwner(user, post);

        post.update(request.title(), request.content());

        int likeCount = postCounterService.getLikeCount(postId);
        int shareCount = postCounterService.getShareCount(postId);

        return PostResponse.from(post, likeCount, shareCount);
    }

    // 게시글 삭제
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "postDetail", key = "#postId"),
            @CacheEvict(value = "postList", allEntries = true)
    })
    public void deletePost(User user, Long postId) {
        Post post = findPostById(postId);
        validatePostOwner(user, post);

        post.softDelete();
    }

    // 공통 유틸 메소드
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new GlobalException(CommunityErrorCode.POST_NOT_FOUND));
    }

    private void validatePostOwner(User user, Post post) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new GlobalException(CommunityErrorCode.ACCESS_DENIED);
        }
    }
}