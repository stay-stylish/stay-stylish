package org.example.staystylish.common.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@staystylish.com}")
    private String from;

    @Async
    public void sendVerificationEmail(String to, String token, String baseUrl) {
        String verifyLink = baseUrl + "/api/v1/auth/verify?token=" + token;
        String subject = "[StayStylish] 이메일 인증을 완료해주세요";
        String html = """
            <h2>StayStylish 이메일 인증</h2>
            <p>아래 버튼을 눌러 인증을 완료하세요 (10분 내 유효)</p>
            <a href="%s">이메일 인증하기</a>
            """.formatted(verifyLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

            log.info("이메일 전송 완료: {}", to);
        } catch (Exception e) {
            log.error("메일 전송 실패", e);
            throw new RuntimeException("메일 전송 실패");
        }
    }
}
