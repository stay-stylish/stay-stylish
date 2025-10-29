package org.example.staystylish.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.staystylish.common.mail.EmailService;
import org.example.staystylish.domain.user.code.UserErrorCode;
import org.example.staystylish.domain.user.entity.User;
import org.example.staystylish.domain.user.exception.UserException;
import org.example.staystylish.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private static final String PREFIX = "email:verify:";
    private static final Duration TTL = Duration.ofMinutes(10);

    /** 회원가입 후 메일 발송 */
    public void issueTokenAndSendMail(User user, String baseUrl) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, user.getEmail(), TTL);
        emailService.sendVerificationEmail(user.getEmail(), token, baseUrl);

        log.info("[이메일 인증] 토큰 발급 및 메일 전송 완료 - {}", user.getEmail());
    }

    /** 이메일 인증 완료 처리 */
    @Transactional
    public void verify(String token) {
        String email = redisTemplate.opsForValue().get(PREFIX + token);

        if (email == null) {
            throw new UserException(UserErrorCode.EMAIL_TOKEN_INVALID);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new UserException(UserErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        user.verifyEmail();
        redisTemplate.delete(PREFIX + token);
        log.info("[이메일 인증 완료] {}", email);
    }

    /** 인증 메일 재발송 */
    @Transactional(readOnly = true)
    public void resend(String email, String baseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new UserException(UserErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        issueTokenAndSendMail(user, baseUrl);
        log.info("[이메일 재발송 완료] {}", email);
    }
}
