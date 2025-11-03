package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String LOCK_PREFIX = "lock:signup:";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

    // 회원가입
    @Transactional
    public UserResponse signup(SignupRequest request, String baseUrl) {
        String email = request.email();
        String lockKey = LOCK_PREFIX + email;

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
