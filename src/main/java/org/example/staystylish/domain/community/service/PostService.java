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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return PostResponse.from(postRepository.save(post));
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = findPostById(postId);

        // Redis에서 실시간 카운트 가져오기
        int likeCount = postCounterService.getLikeCount(postId);
        int shareCount = postCounterService.getShareCount(postId);

        // 임시로 설정 (DB 업데이트 없이)
        post.setLikeCount(likeCount);
        post.setShareCount(shareCount);

        return PostResponse.from(post);
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable, String sortBy) {
        log.info("===== getAllPosts 시작 - sortBy: {}, page: {}, size: {} =====",
                sortBy, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 모든 게시글 가져오기 (정렬 없이)
        Page<Post> posts = postRepository.findAllOrderByLatest(pageable);
        log.info("DB에서 가져온 게시글 수: {}", posts.getContent().size());

        // 2. Redis 카운트를 각 게시글에 적용
        List<Post> postList = posts.getContent();
        for (Post post : postList) {
            int dbLikeCount = post.getLikeCount();
            int redisLikeCount = postCounterService.getLikeCount(post.getId());
            int shareCount = postCounterService.getShareCount(post.getId());

            log.info("Post ID: {}, DB likeCount: {}, Redis likeCount: {}",
                    post.getId(), dbLikeCount, redisLikeCount);

            post.setLikeCount(redisLikeCount);
            post.setShareCount(shareCount);
        }

        // 3. 좋아요순 정렬이 요청된 경우 메모리에서 정렬
        if ("like".equalsIgnoreCase(sortBy)) {
            log.info("좋아요순 정렬 시작");

            // 정렬 전 상태 로깅
            for (int i = 0; i < postList.size(); i++) {
                Post p = postList.get(i);
                log.info("정렬 전 [{}] - Post ID: {}, likeCount: {}", i, p.getId(), p.getLikeCount());
            }

            postList.sort((p1, p2) -> {
                int compare = Integer.compare(p2.getLikeCount(), p1.getLikeCount());
                if (compare == 0) {
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                }
                return compare;
            });

            // 정렬 후 상태 로깅
            for (int i = 0; i < postList.size(); i++) {
                Post p = postList.get(i);
                log.info("정렬 후 [{}] - Post ID: {}, likeCount: {}", i, p.getId(), p.getLikeCount());
            }
        }

        // 4. PostResponse로 변환
        List<PostResponse> responseList = postList.stream()
                .map(PostResponse::from)
                .toList();

        log.info("===== getAllPosts 완료 - 반환 게시글 수: {} =====", responseList.size());

        // 5. Page 객체로 반환
        return new org.springframework.data.domain.PageImpl<>(
                responseList,
                pageable,
                posts.getTotalElements()
        );
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
        return PostResponse.from(post);
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


