package com.gahih.domain.admin.dto;

import com.gahih.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminMemberResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String role;
    private final String status;
    private final LocalDateTime createdAt;
    private final Long totalReportCount;
    private final Long pendingReportCount;

    private final LocalDateTime suspendedAt;
    private final LocalDateTime suspendedUntil;
    private final String suspensionReason;
    private final Integer suspensionCount;

    public AdminMemberResponse(
            Long id,
            String username,
            String nickname,
            String email,
            String role,
            String status,
            LocalDateTime createdAt,
            Long totalReportCount,
            Long pendingReportCount,
            LocalDateTime suspendedAt,
            LocalDateTime suspendedUntil,
            String suspensionReason,
            Integer suspensionCount
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.totalReportCount = totalReportCount;
        this.pendingReportCount = pendingReportCount;
        this.suspendedAt = suspendedAt;
        this.suspendedUntil = suspendedUntil;
        this.suspensionReason = suspensionReason;
        this.suspensionCount = suspensionCount;
    }

    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getEmail(),
                member.getRole().name(),
                member.getStatus().name(),
                member.getCreatedAt(),
                0L,
                0L,
                member.getSuspendedAt(),
                member.getSuspendedUntil(),
                member.getSuspensionReason(),
                member.getSuspensionCount()
        );
    }

    public boolean isCanFinalize() {
        return !"DELETED".equals(status) && !"ADMIN".equals(role);
    }

    public String getDisplayNickname() {
        if ("DELETED".equals(status)) {
            return "최종 종료된 회원";
        }
        return nickname;
    }

    public String getDisplayEmail() {
        if ("DELETED".equals(status)) {
            return "비식별화 완료";
        }
        return email;
    }

    public Long getSafeTotalReportCount() {
        return totalReportCount == null ? 0L : totalReportCount;
    }

    public Long getSafePendingReportCount() {
        return pendingReportCount == null ? 0L : pendingReportCount;
    }

    public String getReportSummary() {
        return getSafePendingReportCount() + " / " + getSafeTotalReportCount();
    }

    public boolean isTemporarySuspended() {
        return "SUSPENDED".equals(status) && suspendedUntil != null;
    }

    public boolean isPermanentSuspended() {
        return "SUSPENDED".equals(status) && suspendedUntil == null;
    }

    public long getSuspensionRemainingDays() {
        if (!isTemporarySuspended()) {
            return 0L;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDateTime.now(), suspendedUntil);
        return Math.max(days, 0L);
    }

    public String getSuspensionSummary() {
        if (!"SUSPENDED".equals(status)) {
            return "-";
        }

        if (isPermanentSuspended()) {
            return "영구정지";
        }

        return "기간정지 (" + getSuspensionRemainingDays() + "일 남음)";
    }

    public String getSuspensionReasonDisplay() {
        if (suspensionReason == null || suspensionReason.isBlank()) {
            return "-";
        }
        return suspensionReason;
    }

    public int getSafeSuspensionCount() {
        return suspensionCount == null ? 0 : suspensionCount;
    }
}
