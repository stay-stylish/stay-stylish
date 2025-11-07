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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
            // ② 삭제되지 않은 활성 사용자 확인
            if (userRepository.existsByEmailAndNotDeleted(email)) {
                log.warn("중복된 활성 사용자 가입 시도: {}", email);
                throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
            }

            // ③ 탈퇴한 사용자 확인 및 복구
            Optional<User> deletedUserOpt = userRepository.findByEmailIncludeDeleted(email);
            User user;

            if (deletedUserOpt.isPresent()) {
                user = deletedUserOpt.get();

                // 탈퇴한 사용자 확인
                if (user.isDeleted()) {
                    log.info("탈퇴한 계정 복구 시작: {}", email);

                    // 기본정보 업데이트
                    user.updateProfile(
                            request.nickname(),
                            request.stylePreference(),
                            request.gender() != null ? Gender.valueOf(request.gender().toUpperCase()) : null
                    );

                    // 비밀번호 업데이트
                    user.setPassword(passwordEncoder.encode(request.password()));

                    // 삭제 상태 복구
                    user.setDeletedAt(null);

                    // 이메일 인증 상태 초기화
                    user.setEmailVerified(false);

                    userRepository.save(user);

                    log.info("탈퇴한 계정 복구 완료: {}", email);

                    // 인증 메일 발송
                    emailVerificationService.issueTokenAndSendMail(user, baseUrl);

                    return UserResponse.from(user);
                } else {
                    // 이 경우는 existsByEmailAndNotDeleted에서 걸러져야 하지만, 혹시 모르니 처리
                    log.warn("활성 사용자 가입 시도: {}", email);
                    throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
                }
            }

            // ④ 신규 사용자 생성
            user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(request.password()))
                    .nickname(request.nickname())
                    .stylePreference(request.stylePreference())
                    .gender(request.gender() != null ? Gender.valueOf(request.gender().toUpperCase()) : null)
                    .provider(Provider.LOCAL)
                    .emailVerified(false)
                    .build();

            userRepository.save(user);

            // ⑤ 인증 메일 발송
            emailVerificationService.issueTokenAndSendMail(user, baseUrl);
            log.info("신규 회원가입 완료 및 인증 메일 발송: {}", user.getEmail());

            return UserResponse.from(user);

        } finally {
            // ⑥ 락 해제
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
        log.info("[OAuth 교환] ===== 시작 =====");
        log.info("[OAuth 교환] 받은 코드: {}", code);

        // 코드 키 생성
        String codeKey = RedisKeyConstants.oauthCodeKey(code);
        log.info("[OAuth 교환] 코드 키: {}", codeKey);

        // Redis 조회
        log.info("[OAuth 교환] Redis 조회 시작...");
        String tokenJson = redisTemplate.opsForValue().get(codeKey);
        log.info("[OAuth 교환] Redis 조회 결과: {}", tokenJson != null ? "있음" : "없음");

        // DEBUG: Redis에 키가 있는지 확인
        if (tokenJson == null) {
            log.error("[OAuth 교환] 코드를 Redis에서 찾을 수 없습니다!");
            log.error("[OAuth 교환] 코드 키: {}", codeKey);

            // 🔧 디버그: Redis의 모든 키 확인 (테스트용)
            try {
                Set<String> keys = redisTemplate.keys("oauth:*");
                log.error("[OAuth 교환] Redis의 oauth: 키 목록: {}", keys);
                if (keys != null && !keys.isEmpty()) {
                    log.error("[OAuth 교환] 저장된 키 개수: {}", keys.size());
                    keys.forEach(k -> {
                        String value = redisTemplate.opsForValue().get(k);
                        log.error("[OAuth 교환] 키: {}, 값 있음: {}", k, value != null);

                        // TTL 확인
                        Long ttl = redisTemplate.getExpire(k, TimeUnit.SECONDS);
                        log.error("[OAuth 교환] 키: {}, TTL: {} 초", k, ttl);
                    });
                } else {
                    log.error("[OAuth 교환] Redis에 oauth: 로 시작하는 키가 없습니다!");
                }
            } catch (Exception e) {
                log.error("[OAuth 교환] Redis 키 조회 중 오류", e);
            }

            // 가능한 원인들
            log.error("[OAuth 교환] 가능한 원인:");
            log.error("[OAuth 교환]   1. OAuth2SuccessHandler에서 Redis에 저장 안 함");
            log.error("[OAuth 교환]   2. 저장했지만 TTL으로 인해 이미 삭제됨");
            log.error("[OAuth 교환]   3. Redis 연결 오류");
            log.error("[OAuth 교换]   4. 코드 포맷이 다름");

            throw new UserException(UserErrorCode.INVALID_SESSION);
        }

        log.info("[OAuth 교환] Redis에서 코드 조회 성공");
        log.info("[OAuth 교환] 토큰 데이터 (처음 100자): {}", tokenJson.substring(0, Math.min(100, tokenJson.length())));

        try {
            // JSON 파싱
            log.info("[OAuth 교환] JSON 파싱 시작...");
            Map<String, String> tokenData = objectMapper.readValue(tokenJson, TOKEN_DATA_TYPE);
            log.info("[OAuth 교환] JSON 파싱 성공");

            // 필수 필드 검증
            String email = tokenData.get("email");
            String accessToken = tokenData.get("accessToken");
            String refreshToken = tokenData.get("refreshToken");
            String isNewUserStr = tokenData.get("isNewUser");

            log.info("[OAuth 교환] 필드 확인:");
            log.info("[OAuth 교환]   - email: {}", email != null ? "있음" : "없음");
            log.info("[OAuth 교환]   - accessToken: {}", accessToken != null ? "있음" : "없음");
            log.info("[OAuth 교환]   - refreshToken: {}", refreshToken != null ? "있음" : "없음");
            log.info("[OAuth 교환]   - isNewUser: {}", isNewUserStr != null ? isNewUserStr : "없음");

            if (email == null || accessToken == null || refreshToken == null) {
                log.error("[OAuth 교환] 필수 필드 누락");
                log.error("[OAuth 교환] 전체 데이터: {}", tokenData);
                throw new UserException(UserErrorCode.INVALID_SESSION);
            }

            // 일회용 코드 삭제 (사용 완료)
            log.info("[OAuth 교환] 코드 삭제 중... 키: {}", codeKey);
            Boolean deleted = redisTemplate.delete(codeKey);
            log.info("[OAuth 교환] 코드 삭제 결과: {}", deleted ? "성공" : "실패");

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("isNewUser", Boolean.parseBoolean(isNewUserStr));

            log.info("[OAuth 교환] 교환 완료");
            log.info("[OAuth 교환] 이메일: {}", email);
            log.info("[OAuth 교환] isNewUser: {}", isNewUserStr);
            log.info("[OAuth 교환] ===== 성공 =====");

            return response;

        } catch (JsonProcessingException e) {
            // JSON 파싱 오류
            log.error("[OAuth 교환] JSON 파싱 오류", e);
            log.error("[OAuth 교환] 원본 JSON: {}", tokenJson);
            throw new UserException(UserErrorCode.INVALID_SESSION);

        } catch (UserException e) {
            // 비즈니스 로직 예외는 그대로 재던지기
            log.error("[OAuth 교환] 비즈니스 예외: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            // 예기치 않은 오류
            log.error("[OAuth 교환] 예기치 않은 오류", e);
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