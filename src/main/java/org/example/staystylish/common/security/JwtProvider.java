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

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // JWT 생성
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT에서 사용자 이메일 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증 + 상세 로깅
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 토큰이 만료되었습니다. - {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT] 지원되지 않는 JWT 형식입니다. - {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("[JWT] 잘못된 JWT 형식입니다. - {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("[JWT] 서명이 유효하지 않습니다. - {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[JWT] 토큰이 비어있거나 잘못되었습니다. - {}", e.getMessage());
        } catch (Exception e) {
            log.error("[JWT] 토큰 검증 중 알 수 없는 오류 발생 - {}", e.getMessage(), e);
        }

        return false;
    }
}

