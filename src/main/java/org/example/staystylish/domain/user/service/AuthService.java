package org.example.staystylish.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.constants.RedisKeyConstants;
import org.example.staystylish.common.security.JwtProvider;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.dto.request.LoginRequest;
import org.example.staystylish.domain.user.dto.request.SignupRequest;
import org.example.staystylish.domain.user.dto.response.UserResponse;
import org.example.staystylish.domain.user.entity.Gender;
import org.example.staystylish.domain.user.entity.Provider;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

    // TypeReference 상수화
    private static final TypeReference<Map<String, String>> TOKEN_DATA_TYPE =
            new TypeReference<>() {};

    // 회원가입
    @Transactional
    public UserResponse signup(SignupRequest request, String baseUrl) {
        String email = request.email();
        String lockKey = RedisKeyConstants.signupLockKey(email);

        // ① 락 획득 시도
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", LOCK_TIMEOUT);
        if (Boolean.FALSE.equals(acquired)) {
            throw new UserException(UserErrorCode.SIGNUP_IN_PROGRESS);
        }

        try {
            // ② 이메일 중복 체크
            if (userRepository.existsByEmail(email)) {
                throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
            }

            // ③ 사용자 생성
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(request.password()))
                    .nickname(request.nickname())
                    .stylePreference(request.stylePreference())
                    .gender(request.gender() != null ? Gender.valueOf(request.gender().toUpperCase()) : null)
                    .provider(Provider.LOCAL)
                    .emailVerified(false)
                    .build();

            userRepository.save(user);

            // ④ 인증 메일 발송
            emailVerificationService.issueTokenAndSendMail(user, baseUrl);
            log.info("회원가입 완료 및 인증 메일 발송: {}", user.getEmail());

            return UserResponse.from(user);

        } finally {
            // ⑤ 락 해제
            redisTemplate.delete(lockKey);
        }
    }

    // 로그인
    @Transactional(readOnly = true)
    public Map<String, String> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) throw new UserException(UserErrorCode.USER_DELETED);

        if (!user.isEmailVerified())
            throw new UserException(UserErrorCode.EMAIL_NOT_VERIFIED);

        if (!passwordEncoder.matches(request.password(), user.getPassword()))
            throw new UserException(UserErrorCode.INVALID_PASSWORD);

        // Access + Refresh 발급
        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        // Refresh Token Redis에 저장
        refreshTokenService.save(user.getEmail(), refreshToken, jwtProvider.getRefreshTokenValidity());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    // OAuth 코드 교환
    @Transactional(readOnly = true)
    public Map<String, Object> exchangeOAuthCode(String code) {
        String codeKey = RedisKeyConstants.oauthCodeKey(code);
        String tokenJson = redisTemplate.opsForValue().get(codeKey);

        if (tokenJson == null) {
            log.warn("[OAuth 토큰 교환 실패] 유효하지 않거나 만료된 코드: {}", code);
            throw new UserException(UserErrorCode.INVALID_SESSION);
        }

        try {
            // JSON 파싱
            Map<String, String> tokenData = objectMapper.readValue(tokenJson, TOKEN_DATA_TYPE);

            // 필수 필드 검증
            String email = tokenData.get("email");
            String accessToken = tokenData.get("accessToken");
            String refreshToken = tokenData.get("refreshToken");

            if (email == null || accessToken == null || refreshToken == null) {
                log.error("[OAuth 토큰 교환 실패] 필수 필드 누락 - code: {}", code);
                throw new UserException(UserErrorCode.INVALID_SESSION);
            }

            // 일회용 코드 삭제 (사용 완료)
            redisTemplate.delete(codeKey);

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("isNewUser", Boolean.parseBoolean(tokenData.get("isNewUser")));

            log.info("[OAuth 토큰 교환 완료] email: {}", email);

            return response;

        } catch (JsonProcessingException e) {
            // JSON 파싱 오류 구체적 처리
            log.error("[OAuth 토큰 교환 실패] JSON 파싱 오류 - code: {}, json: {}", code, tokenJson, e);
            throw new UserException(UserErrorCode.INVALID_SESSION);

        } catch (UserException e) {
            // 비즈니스 로직 예외는 그대로 재던지기
            throw e;

        } catch (Exception e) {
            // 예기치 않은 오류 별도 처리 (모니터링/알림 대상)
            log.error("[OAuth 토큰 교환 실패] 예기치 않은 오류 발생 - code: {}", code, e);
            throw new UserException(UserErrorCode.INVALID_SESSION);
        }
    }

    // Refresh 재발급
    @Transactional(readOnly = true)
    public Map<String, String> reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new UserException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtProvider.getUsername(refreshToken);
        String savedToken = refreshTokenService.get(email);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new UserException(UserErrorCode.INVALID_SESSION);
        }

        // 새 Access 토큰 발급
        String newAccessToken = jwtProvider.generateAccessToken(email);

        return Map.of("accessToken", newAccessToken);
    }

    @Transactional
    public void logout(String email) {
        refreshTokenService.delete(email);
    }
}