package com.gahih.domain.member.service.email;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Component
public class EmailAuthPolicy {

    @Value("${app.email-auth.code-expiration-seconds:180}")
    private long codeExpirationSeconds;

    @Value("${app.email-auth.resend-interval-seconds:30}")
    private long resendIntervalSeconds;

    @Value("${app.email-auth.max-request-count:5}")
    private int maxRequestCount;

    @Value("${app.email-auth.password-reset-session-expiration-seconds:600}")
    private long passwordResetSessionExpirationSeconds;

    public LocalDateTime calculateCodeExpiresAt() {
        return LocalDateTime.now().plusSeconds(codeExpirationSeconds);
    }

    public LocalDateTime calculatePasswordResetSessionExpiresAt() {
        return LocalDateTime.now().plusSeconds(passwordResetSessionExpirationSeconds);
    }
}