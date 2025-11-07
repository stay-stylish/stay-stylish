package org.example.staystylish.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.constants.RedisKeyConstants;
import org.example.staystylish.domain.user.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.oauth.redirect-uri}")
    private String redirectUri;

    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getUsername();

        // Access + Refresh Token 발급
        String accessToken = jwtProvider.generateAccessToken(email);
        String refreshToken = jwtProvider.generateRefreshToken(email);

        // Redis에 Refresh Token 저장
        refreshTokenService.save(email, refreshToken, jwtProvider.getRefreshTokenValidity());

        // 일회용 코드 생성
        String code = UUID.randomUUID().toString();

        // 토큰 정보를 JSON으로 저장
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("accessToken", accessToken);
        tokenData.put("refreshToken", refreshToken);
        tokenData.put("email", email);
        tokenData.put("isNewUser", String.valueOf(userPrincipal.isNewUser()));

        // Redis에 일회용 코드 저장
        String tokenJson = objectMapper.writeValueAsString(tokenData);
        String codeKey = RedisKeyConstants.oauthCodeKey(code);
        redisTemplate.opsForValue().set(codeKey, tokenJson, CODE_TTL);

        log.info("[OAuth2 Success] 일회용 코드 발급 완료 - email: {}, code: {}, isNewUser: {}",
                email, code, userPrincipal.isNewUser());

        String baseUrl = redirectUri.endsWith("/") ? redirectUri.substring(0, redirectUri.length() - 1) : redirectUri;

        String path = userPrincipal.isNewUser()
                ? "/oauth/success/signup/additional"
                : "/oauth/success/home";

        String redirectUrl = baseUrl + path + "?code=" + code;

        log.info("[OAuth2 Success] redirect URL: {}", redirectUrl);

        log.info("[DEBUG] redirectUri from config: {}", redirectUri);
        log.info("[DEBUG] baseUrl after processing: {}", baseUrl);
        log.info("[DEBUG] path: {}", path);
        log.info("[DEBUG] final redirectUrl: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}