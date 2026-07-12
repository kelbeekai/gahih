package com.gahih.domain.member.dto;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class EmailAuthApiResponse {

    private final boolean success;
    private final String message;
    private final LocalDateTime expiresAt;
    private final boolean verified;

    private EmailAuthApiResponse(boolean success, String message, LocalDateTime expiresAt, boolean verified) {
        this.success = success;
        this.message = message;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public static EmailAuthApiResponse success(String message, LocalDateTime expiresAt, boolean verified) {
        return new EmailAuthApiResponse(true, message, expiresAt, verified);
    }

    public static EmailAuthApiResponse fail(String message) {
        return new EmailAuthApiResponse(false, message, null, false);
    }

    public Long getRemainingSeconds() {
        if (expiresAt == null) {
            return null;
        }

        long seconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return Math.max(seconds, 0L);
    }
}