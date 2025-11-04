package org.example.staystylish.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void incrLike(Long postId) {
        redis.opsForValue().increment(LIKE_KEY + postId);
    }

    public void decrLike(Long postId) {
        redis.opsForValue().decrement(LIKE_KEY + postId);
    }

    public void incrShare(Long postId) {
        redis.opsForValue().increment(SHARE_KEY + postId);
    }

    /** 5분마다 Redis → DB 반영 */
    @Transactional
    @Scheduled(fixedRate = 300000) // 300,000 ms = 5분
    public void syncToDB() {
        syncMetric(LIKE_KEY, true);
        syncMetric(SHARE_KEY, false);
    }

    private void syncMetric(String prefix, boolean like) {
        Set<String> keys = redis.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                Long postId = Long.valueOf(key.substring(prefix.length()));
                String val = redis.opsForValue().get(key);
                if (val == null) continue;

                int count = Integer.parseInt(val);
                postRepository.findById(postId).ifPresent(post -> {
                    if (like) post.setLikeCount(count);
                    else post.setShareCount(count);
                });
            } catch (Exception e) {
                log.warn("카운터 동기화 실패 key={}", key, e);
            }
        }
        // JPA Flush로 일괄 반영
        postRepository.flush();
    }
}
