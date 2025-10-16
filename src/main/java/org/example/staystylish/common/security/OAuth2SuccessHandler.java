package org.example.staystylish.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    // 환경 설정에서 주입받을 리다이렉트 URL
    @Value("${app.oauth.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getUsername();

        // JWT 토큰 발급
        String accessToken = jwtProvider.generateToken(email);
        log.info("[OAuth2 Success] JWT 발급 완료 - email: {}", email);

        // 환경별 redirect URI 적용
        String redirectUrl = redirectUri + "?token=" +
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

        log.info("[OAuth2 Success] redirect: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}
