package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCounterService {

    private final StringRedisTemplate redis;
    private final PostRepository postRepository;

    private static final String LIKE_KEY = "post:like:";
    private static final String SHARE_KEY = "post:share:";
    private static final String LIKE_SORTED_SET = "post:like:sorted";

    private static final String UPDATED_LIKE_SET = "post:update:like";
    private static final String UPDATED_SHARE_SET = "post:update:share";

    public void incrLike(Long postId) {
        redis.opsForValue().increment(LIKE_KEY + postId);
        redis.opsForSet().add(UPDATED_LIKE_SET, String.valueOf(postId));

        // Sorted Set에도 업데이트
        String countStr = redis.opsForValue().get(LIKE_KEY + postId);
        if (countStr != null) {
            redis.opsForZSet().add(LIKE_SORTED_SET, String.valueOf(postId), Double.parseDouble(countStr));
        }
    }

    public void decrLike(Long postId) {
        redis.opsForValue().decrement(LIKE_KEY + postId);
        redis.opsForSet().add(UPDATED_LIKE_SET, String.valueOf(postId));

        // Sorted Set에도 업데이트
        String countStr = redis.opsForValue().get(LIKE_KEY + postId);
        if (countStr != null) {
            redis.opsForZSet().add(LIKE_SORTED_SET, String.valueOf(postId), Double.parseDouble(countStr));
        }
    }

    public void incrShare(Long postId) {
        redis.opsForValue().increment(SHARE_KEY + postId);
        redis.opsForSet().add(UPDATED_SHARE_SET, String.valueOf(postId));
    }

    // 게시글 생성 시 Sorted Set 초기화
    public void initializeCounts(Long postId, int likeCount, int shareCount) {
        redis.opsForValue().set(LIKE_KEY + postId, String.valueOf(likeCount));
        redis.opsForValue().set(SHARE_KEY + postId, String.valueOf(shareCount));
        redis.opsForZSet().add(LIKE_SORTED_SET, String.valueOf(postId), (double) likeCount);
    }

    public int getLikeCount(Long postId) {
        String countStr = redis.opsForValue().get(LIKE_KEY + postId);
        if (countStr == null) {
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                int dbCount = post.getLikeCount();
                redis.opsForValue().set(LIKE_KEY + postId, String.valueOf(dbCount));
                redis.opsForZSet().add(LIKE_SORTED_SET, String.valueOf(postId), (double) dbCount);
                return dbCount;
            }
            return 0;
        }
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.warn("[PostCounter] 좋아요 카운트 파싱 실패 postId={}", postId, e);
            return 0;
        }
    }

    public int getShareCount(Long postId) {
        String countStr = redis.opsForValue().get(SHARE_KEY + postId);
        if (countStr == null) {
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                int dbCount = post.getShareCount();
                redis.opsForValue().set(SHARE_KEY + postId, String.valueOf(dbCount));
                return dbCount;
            }
            return 0;
        }
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.warn("[PostCounter] 공유 카운트 파싱 실패 postId={}", postId, e);
            return 0;
        }
    }

    // 좋아요순으로 정렬된 게시글 ID 목록 가져오기
    public List<Long> getPostIdsSortedByLike(int page, int size) {
        int start = page * size;
        int end = start + size - 1;

        // ZREVRANGE: 높은 점수부터 (좋아요 많은 순)
        Set<String> postIdStrs = redis.opsForZSet().reverseRange(LIKE_SORTED_SET, start, end);

        if (postIdStrs == null || postIdStrs.isEmpty()) {
            return List.of();
        }

        return postIdStrs.stream()
                .map(Long::parseLong)
                .toList();
    }

    // 전체 게시글 수 (Sorted Set 크기)
    public long getTotalPostCount() {
        Long size = redis.opsForZSet().size(LIKE_SORTED_SET);
        return size != null ? size : 0;
    }

    @Transactional
    @Scheduled(fixedRate = 300000) // 5분
    public void syncToDB() {
        syncMetric(UPDATED_LIKE_SET, LIKE_KEY, true);
        syncMetric(UPDATED_SHARE_SET, SHARE_KEY, false);

        postRepository.flush();
        log.info("[PostCounter Sync] 동기화 완료 (LIKE + SHARE)");
    }

    private void syncMetric(String updateSetKey, String counterPrefix, boolean like) {
        Set<String> postIds = redis.opsForSet().members(updateSetKey);
        if (postIds == null || postIds.isEmpty()) return;

        log.info("[PostCounter Sync] {}개 항목 동기화 시작 ({})",
                postIds.size(), like ? "LIKE" : "SHARE");

        for (String id : postIds) {
            try {
                Long postId = Long.parseLong(id);
                String countStr = redis.opsForValue().get(counterPrefix + postId);
                if (countStr == null) continue;

                int count = Integer.parseInt(countStr);
                postRepository.findById(postId).ifPresent(post -> {
                    if (like) {
                        post.setLikeCount(count);
                        // Sorted Set도 동기화
                        redis.opsForZSet().add(LIKE_SORTED_SET, String.valueOf(postId), (double) count);
                    } else {
                        post.setShareCount(count);
                    }
                });

            } catch (NumberFormatException e) {
                log.warn("[PostCounter Sync] 숫자 변환 실패 id={} ({})", id, like ? "LIKE" : "SHARE", e);
            } catch (Exception e) {
                log.warn("[PostCounter Sync] 동기화 실패 id={} ({})", id, like ? "LIKE" : "SHARE", e);
            }
        }

        redis.delete(updateSetKey);
    }
}
