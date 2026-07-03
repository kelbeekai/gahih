package com.gahih.domain.member.entity;

import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nickname_reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameReservation {

    private static final int NICKNAME_MAX_LENGTH = 50;
    private static final int REASON_TYPE_MAX_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
    private String nickname;

    @Column(nullable = false, length = REASON_TYPE_MAX_LENGTH)
    private String reasonType;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private NicknameReservation(
            String nickname,
            String reasonType,
            LocalDateTime expiresAt
    ) {
        validateNickname(nickname);
        validateReasonType(reasonType);
        validateExpiresAt(expiresAt);

        this.nickname = nickname;
        this.reasonType = reasonType;
        this.expiresAt = expiresAt;
    }

    public static NicknameReservation reserveForNicknameChange(String nickname, long blockDays) {
        return new NicknameReservation(
                nickname,
                "NICKNAME_CHANGED",
                LocalDateTime.now().plusDays(blockDays)
        );
    }

    public static NicknameReservation reserveForMemberFinalized(String nickname, long blockDays) {
        return new NicknameReservation(
                nickname,
                "MEMBER_FINALIZED",
                LocalDateTime.now().plusDays(blockDays)
        );
    }

    public static NicknameReservation reserveForAdminForcedNickname(String nickname, long blockDays) {
        return new NicknameReservation(
                nickname,
                "ADMIN_FORCED",
                LocalDateTime.now().plusDays(blockDays)
        );
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new DomainValidationException("예약할 닉네임은 비어 있을 수 없습니다.");
        }
        if (nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new DomainValidationException("예약할 닉네임은 50자를 초과할 수 없습니다.");
        }
    }

    private void validateReasonType(String reasonType) {
        if (reasonType == null || reasonType.isBlank()) {
            throw new DomainValidationException("닉네임 예약 사유 타입은 필수입니다.");
        }
        if (reasonType.length() > REASON_TYPE_MAX_LENGTH) {
            throw new DomainValidationException("닉네임 예약 사유 타입은 30자를 초과할 수 없습니다.");
        }
    }

    private void validateExpiresAt(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new DomainValidationException("닉네임 예약 만료 시각은 필수입니다.");
        }
        if (!expiresAt.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("닉네임 예약 만료 시각은 현재보다 미래여야 합니다.");
        }
    }
}