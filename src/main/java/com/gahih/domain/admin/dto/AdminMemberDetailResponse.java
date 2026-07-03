package com.gahih.domain.admin.dto;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminMemberDetailResponse {

    private final Long id;
    private final String username;
    private final String displayNickname;
    private final String displayEmail;
    private final String role;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime nicknameChangedAt;
    private final LocalDateTime withdrawnAt;
    private final LocalDateTime withdrawExpireAt;
    private final LocalDateTime finalizedAt;

    private final LocalDateTime suspendedAt;
    private final LocalDateTime suspendedUntil;
    private final String suspensionReason;
    private final Integer suspensionCount;
    private final long suspensionRemainingDays;
    private final boolean temporarySuspended;
    private final boolean permanentSuspended;

    private final boolean canFinalize;
    private final boolean deleted;
    private final boolean withdrawn;
    private final List<AdminMemberDetailPostResponse> recentPosts;
    private final List<AdminMemberDetailCommentResponse> recentComments;

    private AdminMemberDetailResponse(
            Long id,
            String username,
            String displayNickname,
            String displayEmail,
            String role,
            String status,
            LocalDateTime createdAt,
            LocalDateTime nicknameChangedAt,
            LocalDateTime withdrawnAt,
            LocalDateTime withdrawExpireAt,
            LocalDateTime finalizedAt,
            LocalDateTime suspendedAt,
            LocalDateTime suspendedUntil,
            String suspensionReason,
            Integer suspensionCount,
            long suspensionRemainingDays,
            boolean temporarySuspended,
            boolean permanentSuspended,
            boolean canFinalize,
            boolean deleted,
            boolean withdrawn,
            List<AdminMemberDetailPostResponse> recentPosts,
            List<AdminMemberDetailCommentResponse> recentComments
    ) {
        this.id = id;
        this.username = username;
        this.displayNickname = displayNickname;
        this.displayEmail = displayEmail;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.nicknameChangedAt = nicknameChangedAt;
        this.withdrawnAt = withdrawnAt;
        this.withdrawExpireAt = withdrawExpireAt;
        this.finalizedAt = finalizedAt;
        this.suspendedAt = suspendedAt;
        this.suspendedUntil = suspendedUntil;
        this.suspensionReason = suspensionReason;
        this.suspensionCount = suspensionCount;
        this.suspensionRemainingDays = suspensionRemainingDays;
        this.temporarySuspended = temporarySuspended;
        this.permanentSuspended = permanentSuspended;
        this.canFinalize = canFinalize;
        this.deleted = deleted;
        this.withdrawn = withdrawn;
        this.recentPosts = recentPosts;
        this.recentComments = recentComments;
    }

    public static AdminMemberDetailResponse from(
            Member member,
            List<AdminMemberDetailPostResponse> recentPosts,
            List<AdminMemberDetailCommentResponse> recentComments
    ) {
        return new AdminMemberDetailResponse(
                member.getId(),
                member.getUsername(),
                resolveDisplayNickname(member),
                resolveDisplayEmail(member),
                member.getRole().name(),
                member.getStatus().name(),
                member.getCreatedAt(),
                member.getNicknameChangedAt(),
                member.getWithdrawnAt(),
                member.getWithdrawExpireAt(),
                member.getFinalizedAt(),
                member.getSuspendedAt(),
                member.getSuspendedUntil(),
                member.getSuspensionReason(),
                member.getSuspensionCount(),
                member.getSuspensionRemainingDays(),
                member.isTemporarySuspended(),
                member.isPermanentSuspended(),
                member.getRole().name().equals("USER") && !member.isDeleted(),
                member.isDeleted(),
                member.isWithdrawn(),
                recentPosts,
                recentComments
        );
    }

    private static String resolveDisplayNickname(Member member) {

        if (member.getStatus() == MemberStatus.DELETED) {
            return "비식별 처리됨";
        }
        return member.getNickname();
    }

    private static String resolveDisplayEmail(Member member) {
        if (member.getStatus() == MemberStatus.DELETED) {
            return "비식별 처리됨";
        }
        return member.getEmail();
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