package com.gahih.domain.member.dto;

import com.gahih.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberMyPageResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String role;
    private final String status;
    private final boolean withdrawAllowed;

    private final boolean suspended;
    private final boolean temporarySuspended;
    private final boolean permanentSuspended;
    private final LocalDateTime suspendedAt;
    private final LocalDateTime suspendedUntil;
    private final long suspensionRemainingDays;
    private final String suspensionReason;

    public MemberMyPageResponse(
            Long id,
            String username,
            String nickname,
            String email,
            String role,
            String status,
            boolean withdrawAllowed,
            boolean suspended,
            boolean temporarySuspended,
            boolean permanentSuspended,
            LocalDateTime suspendedAt,
            LocalDateTime suspendedUntil,
            long suspensionRemainingDays,
            String suspensionReason
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.withdrawAllowed = withdrawAllowed;
        this.suspended = suspended;
        this.temporarySuspended = temporarySuspended;
        this.permanentSuspended = permanentSuspended;
        this.suspendedAt = suspendedAt;
        this.suspendedUntil = suspendedUntil;
        this.suspensionRemainingDays = suspensionRemainingDays;
        this.suspensionReason = suspensionReason;
    }

    public static MemberMyPageResponse of(
            Long id,
            String username,
            String nickname,
            String email,
            String role,
            String status,
            boolean withdrawAllowed,
            boolean suspended,
            boolean temporarySuspended,
            boolean permanentSuspended,
            LocalDateTime suspendedAt,
            LocalDateTime suspendedUntil,
            long suspensionRemainingDays,
            String suspensionReason
    ) {
        return new MemberMyPageResponse(
                id,
                username,
                nickname,
                email,
                role,
                status,
                withdrawAllowed,
                suspended,
                temporarySuspended,
                permanentSuspended,
                suspendedAt,
                suspendedUntil,
                suspensionRemainingDays,
                suspensionReason
        );
    }

    public static MemberMyPageResponse from(Member member) {
        return of(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getEmail(),
                member.getRole().name(),
                member.getStatus().name(),
                member.isActive(),
                member.isSuspended(),
                member.isTemporarySuspended(),
                member.isPermanentSuspended(),
                member.getSuspendedAt(),
                member.getSuspendedUntil(),
                member.getSuspensionRemainingDays(),
                member.getSuspensionReason()
        );
    }

    public String getSuspensionReasonDisplay() {
        if (suspensionReason == null || suspensionReason.isBlank()) {
            return "운영 정책 위반";
        }
        return suspensionReason;
    }
}