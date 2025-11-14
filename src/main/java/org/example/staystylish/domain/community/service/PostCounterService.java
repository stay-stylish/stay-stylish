package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.domain.community.entity.Post;
import org.example.staystylish.domain.community.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCounterService {

    private final StringRedisTemplate redis;
    private final PostRepository postRepository;

    private static final String LIKE_KEY = "post:like:";
    private static final String SHARE_KEY = "post:share:";

    private static final String UPDATED_LIKE_SET = "post:update:like";
    private static final String UPDATED_SHARE_SET = "post:update:share";

    public void incrLike(Long postId) {
        redis.opsForValue().increment(LIKE_KEY + postId);
        redis.opsForSet().add(UPDATED_LIKE_SET, String.valueOf(postId));
    }

    public void decrLike(Long postId) {
        redis.opsForValue().decrement(LIKE_KEY + postId);
        redis.opsForSet().add(UPDATED_LIKE_SET, String.valueOf(postId));
    }

    public void incrShare(Long postId) {
        redis.opsForValue().increment(SHARE_KEY + postId);
        redis.opsForSet().add(UPDATED_SHARE_SET, String.valueOf(postId));
    }

    public int getLikeCount(Long postId) {
        String countStr = redis.opsForValue().get(LIKE_KEY + postId);
        log.info("Redis 조회 - postId: {}, key: {}, value: {}", postId, LIKE_KEY + postId, countStr);

        if (countStr == null) {
            // Redis에 값이 없으면 DB에서 조회해서 Redis에 설정
            return postRepository.findById(postId)
                    .map(post -> {
                        int dbCount = post.getLikeCount();
                        redis.opsForValue().set(LIKE_KEY + postId, String.valueOf(dbCount));
                        log.info("Redis에 값 없음 - DB에서 조회 후 설정: postId={}, count={}", postId, dbCount);
                        return dbCount;
                    })
                    .orElse(0);
        }
        try {
            int count = Integer.parseInt(countStr);
            log.info("Redis 값 파싱 성공 - postId: {}, count: {}", postId, count);
            return count;
        } catch (NumberFormatException e) {
            log.warn("[PostCounter] 좋아요 카운트 파싱 실패 postId={}", postId, e);
            return 0;
        }
    }

    public int getShareCount(Long postId) {
        return getCount(postId, SHARE_KEY, Post::getShareCount, "공유");
    }

    private int getCount(Long postId, String keyPrefix, java.util.function.Function<Post, Integer> dbCountExtractor, String metricName) {
        String countStr = redis.opsForValue().get(keyPrefix + postId);
        if (countStr == null) {
            // Redis에 값이 없으면 DB에서 조회해서 Redis에 설정
            return postRepository.findById(postId)
                    .map(post -> {
                        int dbCount = dbCountExtractor.apply(post);
                        redis.opsForValue().set(keyPrefix + postId, String.valueOf(dbCount));
                        return dbCount;
                    })
                    .orElse(0);
        }
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.warn("[PostCounter] {} 카운트 파싱 실패 postId={}", metricName, postId, e);
            return 0;
        }
    }

    @Transactional
    @Scheduled(fixedRate = 300000) // 300,000ms = 5분
    public void syncToDB() {
        syncMetric(UPDATED_LIKE_SET, LIKE_KEY, true);
        syncMetric(UPDATED_SHARE_SET, SHARE_KEY, false);

        // flush는 한 번만 호출 (트랜잭션 단위로 일괄 반영)
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
                Long postId = Long.parseLong(id); // 문자열 → Long 변환
                String countStr = redis.opsForValue().get(counterPrefix + postId);
                if (countStr == null) continue;

                int count = Integer.parseInt(countStr);
                postRepository.findById(postId).ifPresent(post -> {
                    if (like) post.setLikeCount(count);
                    else post.setShareCount(count);
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
