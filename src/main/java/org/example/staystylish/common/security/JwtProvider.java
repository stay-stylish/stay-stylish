package org.example.staystylish.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenValidity;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /** ✅ Access Token 발급 */
    public String generateAccessToken(String username) {
        return buildToken(username, accessTokenValidity);
    }

    /** ✅ Refresh Token 발급 */
    public String generateRefreshToken(String username) {
        return buildToken(username, refreshTokenValidity);
    }

    /** 내부 공통 빌더 */
    private String buildToken(String username, long validityMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("[JWT] 유효하지 않은 토큰 - {}", e.getMessage());
            return false;
        }
    }

    // ✅ Refresh 토큰 유효시간 반환용 (Redis TTL 설정용)
    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }
}
