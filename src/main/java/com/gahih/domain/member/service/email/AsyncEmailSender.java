package com.gahih.domain.member.service.email;

import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEmailSender {

    private final EmailSender emailSender;

    @Async("emailTaskExecutor")
    public void sendVerificationCodeAsync(
            String email,
            String code,
            EmailAuthPurpose purpose,
            LocalDateTime expiresAt
    ) {
        try {
            emailSender.sendVerificationCode(email, code, purpose, expiresAt);
            log.info("[email-async] verification mail sent. purpose={}, email={}", purpose, maskEmail(email));
        } catch (Exception e) {
            log.error("[email-async] verification mail failed. purpose={}, email={}, message={}",
                    purpose, maskEmail(email), e.getMessage(), e);
        }
    }

    @Async("emailTaskExecutor")
    public void sendMaskedUsernameAsync(String email, String maskedUsername) {
        try {
            emailSender.sendMaskedUsername(email, maskedUsername);
            log.info("[email-async] username recovery mail sent. email={}", maskEmail(email));
        } catch (Exception e) {
            log.error("[email-async] username recovery mail failed. email={}, message={}",
                    maskEmail(email), e.getMessage(), e);
        }
    }

    @Async("emailTaskExecutor")
    public void sendPasswordResetReadyNoticeAsync(String email) {
        try {
            emailSender.sendPasswordResetReadyNotice(email);
            log.info("[email-async] password reset ready notice sent. email={}", maskEmail(email));
        } catch (Exception e) {
            log.error("[email-async] password reset ready notice failed. email={}, message={}",
                    maskEmail(email), e.getMessage(), e);
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "(blank)";
        }

        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        String visible = localPart.substring(0, Math.min(2, localPart.length()));
        return visible + "***" + domain;
    }
}