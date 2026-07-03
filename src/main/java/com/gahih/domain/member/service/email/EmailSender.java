package com.gahih.domain.member.service.email;

import com.gahih.domain.member.enumtype.EmailAuthPurpose;

import java.time.LocalDateTime;

public interface EmailSender {

    void sendVerificationCode(String email, String code, EmailAuthPurpose purpose, LocalDateTime expiresAt);

    void sendMaskedUsername(String email, String maskedUsername);

    void sendPasswordResetReadyNotice(String email);
}