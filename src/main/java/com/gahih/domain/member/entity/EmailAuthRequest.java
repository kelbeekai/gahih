package com.gahih.domain.member.entity;

import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "email_auth_request",
        indexes = {
                @Index(name = "idx_email_auth_email_purpose_created_at", columnList = "email,purpose,createdAt"),
                @Index(name = "idx_email_auth_target_member_id", columnList = "targetMemberId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailAuthRequest {

    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int CODE_HASH_MAX_LENGTH = 255;
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int MAX_ATTEMPT_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = EMAIL_MAX_LENGTH)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmailAuthPurpose purpose;

    @Column(nullable = false, length = CODE_HASH_MAX_LENGTH)
    private String codeHash;

    @Column(length = USERNAME_MAX_LENGTH)
    private String targetUsername;

    private Long targetMemberId;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private Integer requestCount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private LocalDateTime lastRequestedAt;

    private LocalDateTime createdAt;

    private EmailAuthRequest(
            String email,
            EmailAuthPurpose purpose,
            String codeHash,
            String targetUsername,
            Long targetMemberId,
            LocalDateTime expiresAt,
            Integer requestCount
    ) {
        validateEmail(email);
        validatePurpose(purpose);
        validateCodeHash(codeHash);
        validateExpiresAt(expiresAt);
        validateRequestCount(requestCount);

        this.email = normalizeEmail(email);
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.targetUsername = normalizeUsername(targetUsername);
        this.targetMemberId = targetMemberId;
        this.attemptCount = 0;
        this.requestCount = requestCount;
        this.expiresAt = expiresAt;
        this.lastRequestedAt = LocalDateTime.now();
    }

    public static EmailAuthRequest create(
            String email,
            EmailAuthPurpose purpose,
            String codeHash,
            String targetUsername,
            Long targetMemberId,
            LocalDateTime expiresAt,
            Integer requestCount
    ) {
        return new EmailAuthRequest(
                email,
                purpose,
                codeHash,
                targetUsername,
                targetMemberId,
                expiresAt,
                requestCount
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.lastRequestedAt == null) {
            this.lastRequestedAt = LocalDateTime.now();
        }
        if (this.attemptCount == null) {
            this.attemptCount = 0;
        }
        if (this.requestCount == null) {
            this.requestCount = 1;
        }
    }

    public void increaseAttemptCount() {
        if (isVerified()) {
            throw new DomainValidationException("이미 인증 완료된 요청입니다.");
        }
        this.attemptCount = (this.attemptCount == null ? 0 : this.attemptCount) + 1;
    }

    public boolean increaseAttemptCountAndCheckLimitExceeded() {
        increaseAttemptCount();
        return isAttemptLimitExceeded();
    }

    public void verify() {
        if (isExpired()) {
            throw new DomainValidationException("만료된 인증 요청입니다.");
        }
        if (isVerified()) {
            throw new DomainValidationException("이미 인증 완료된 요청입니다.");
        }
        if (isAttemptLimitExceeded()) {
            throw new DomainValidationException("인증 시도 가능 횟수를 초과했습니다.");
        }

        this.verifiedAt = LocalDateTime.now();
    }

    public void renew(String newCodeHash, LocalDateTime newExpiresAt, Integer newRequestCount) {
        validateCodeHash(newCodeHash);
        validateExpiresAt(newExpiresAt);
        validateRequestCount(newRequestCount);

        this.codeHash = newCodeHash;
        this.expiresAt = newExpiresAt;
        this.requestCount = newRequestCount;
        this.attemptCount = 0;
        this.verifiedAt = null;
        this.lastRequestedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public boolean isAttemptLimitExceeded() {
        return attemptCount != null && attemptCount >= MAX_ATTEMPT_COUNT;
    }

    public boolean isSamePurpose(EmailAuthPurpose purpose) {
        return this.purpose == purpose;
    }

    public boolean isOwnedByMember(Long memberId) {
        if (memberId == null || targetMemberId == null) {
            return false;
        }
        return targetMemberId.equals(memberId);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("이메일은 필수입니다.");
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            throw new DomainValidationException("이메일 길이가 너무 깁니다.");
        }
    }

    private void validatePurpose(EmailAuthPurpose purpose) {
        if (purpose == null) {
            throw new DomainValidationException("이메일 인증 목적은 필수입니다.");
        }
    }

    private void validateCodeHash(String codeHash) {
        if (codeHash == null || codeHash.isBlank()) {
            throw new DomainValidationException("인증코드 해시는 필수입니다.");
        }
        if (codeHash.length() > CODE_HASH_MAX_LENGTH) {
            throw new DomainValidationException("인증코드 해시 길이가 너무 깁니다.");
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

    private void validateRequestCount(Integer requestCount) {
        if (requestCount == null || requestCount < 1) {
            throw new DomainValidationException("요청 횟수는 1 이상이어야 합니다.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return username.trim().toLowerCase();
    }
}