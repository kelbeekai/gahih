package com.gahih.domain.member.entity;

import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "password_reset_session",
        indexes = {
                @Index(name = "idx_password_reset_member_id", columnList = "memberId"),
                @Index(name = "idx_password_reset_email_created_at", columnList = "email,createdAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetSession {

    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int TOKEN_HASH_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(nullable = false, length = TOKEN_HASH_MAX_LENGTH)
    private String resetTokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;

    private PasswordResetSession(
            Long memberId,
            String email,
            String resetTokenHash,
            LocalDateTime expiresAt
    ) {
        validateMemberId(memberId);
        validateEmail(email);
        validateResetTokenHash(resetTokenHash);
        validateExpiresAt(expiresAt);

        this.memberId = memberId;
        this.email = normalizeEmail(email);
        this.resetTokenHash = resetTokenHash;
        this.expiresAt = expiresAt;
    }

    public static PasswordResetSession create(
            Long memberId,
            String email,
            String resetTokenHash,
            LocalDateTime expiresAt
    ) {
        return new PasswordResetSession(memberId, email, resetTokenHash, expiresAt);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void markUsed() {
        if (isUsed()) {
            throw new DomainValidationException("이미 사용된 비밀번호 재설정 세션입니다.");
        }
        if (isExpired()) {
            throw new DomainValidationException("만료된 비밀번호 재설정 세션입니다.");
        }
        this.usedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isAvailable() {
        return !isUsed() && !isExpired();
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null || memberId < 1L) {
            throw new DomainValidationException("회원 ID는 1 이상이어야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("이메일은 필수입니다.");
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            throw new DomainValidationException("이메일 길이가 너무 깁니다.");
        }
    }

    private void validateResetTokenHash(String resetTokenHash) {
        if (resetTokenHash == null || resetTokenHash.isBlank()) {
            throw new DomainValidationException("비밀번호 재설정 토큰 해시는 필수입니다.");
        }
        if (resetTokenHash.length() > TOKEN_HASH_MAX_LENGTH) {
            throw new DomainValidationException("비밀번호 재설정 토큰 해시 길이가 너무 깁니다.");
        }
    }

    private void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new DomainValidationException("만료 시각은 필수입니다.");
        }
        if (!expiresAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("만료 시각은 현재 시각 이후여야 합니다.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}