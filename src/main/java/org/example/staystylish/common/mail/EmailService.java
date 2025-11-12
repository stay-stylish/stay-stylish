package org.example.staystylish.common.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:no-reply@staystylish.com}")
    private String from;

    /**
     * 인증 메일 전송 (비동기)
     */
    @Async("mailExecutor")
    public void sendVerificationEmail(String to, String token, String baseUrl) {
        MDC.put("email", to);
        try {
            String verifyLink = baseUrl + "/auth/verify?token=" + token;
            Context context = new Context();
            context.setVariable("verifyLink", verifyLink);
            String html = templateEngine.process("mail/verify", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject("[StayStylish] 이메일 인증을 완료해주세요");
            helper.setText(html, true);

            mailSender.send(message);
            log.info("인증 메일 발송 완료 (to={})", to);

        } catch (Exception e) {
            log.error("이메일 발송 실패 (to={})", to, e);
        } finally {
            MDC.clear();
        }
    }
}
