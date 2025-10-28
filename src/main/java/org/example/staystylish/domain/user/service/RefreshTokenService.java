package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "refresh:";

    public void save(String email, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(PREFIX + email, refreshToken, Duration.ofMillis(ttlMillis));
    }

    public String get(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    public void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}

