package com.gahih.domain.report.dto;

import com.gahih.domain.report.enumtype.ReportStatus;
import com.gahih.domain.report.enumtype.ReportTargetType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminReportResponse {

    private final Long id;
    private final ReportTargetType targetType;
    private final Long targetId;
    private final ReportStatus status;
    private final Integer currentCycle;
    private final Long totalReportCount;
    private final Long pendingReportCount;
    private final String targetNameSnapshot;
    private final String writerNicknameSnapshot;
    private final Long parentPostId;
    private final String parentPostTitleSnapshot;
    private final String communityCode;
    private final String communityName;
    private final LocalDateTime lastReportedAt;
    private final LocalDateTime lastHandledAt;
    private final Long lastHandledByMemberId;
    private final String lastDecisionReason;

    public AdminReportResponse(
            Long id,
            ReportTargetType targetType,
            Long targetId,
            ReportStatus status,
            Integer currentCycle,
            Long totalReportCount,
            Long pendingReportCount,
            String targetNameSnapshot,
            String writerNicknameSnapshot,
            Long parentPostId,
            String parentPostTitleSnapshot,
            String communityCode,
            String communityName,
            LocalDateTime lastReportedAt,
            LocalDateTime lastHandledAt,
            Long lastHandledByMemberId,
            String lastDecisionReason
    ) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = status;
        this.currentCycle = currentCycle;
        this.totalReportCount = totalReportCount;
        this.pendingReportCount = pendingReportCount;
        this.targetNameSnapshot = targetNameSnapshot;
        this.writerNicknameSnapshot = writerNicknameSnapshot;
        this.parentPostId = parentPostId;
        this.parentPostTitleSnapshot = parentPostTitleSnapshot;
        this.communityCode = communityCode;
        this.communityName = communityName;
        this.lastReportedAt = lastReportedAt;
        this.lastHandledAt = lastHandledAt;
        this.lastHandledByMemberId = lastHandledByMemberId;
        this.lastDecisionReason = lastDecisionReason;
    }

    public boolean isMemberTarget() {
        return targetType == ReportTargetType.MEMBER;
    }

    public boolean isPostTarget() {
        return targetType == ReportTargetType.POST;
    }

    public boolean isCommentTarget() {
        return targetType == ReportTargetType.COMMENT;
    }

    public boolean isAttachmentTarget() {
        return targetType == ReportTargetType.ATTACHMENT;
    }

    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }

    public String getDisplayTargetName() {
        if (targetNameSnapshot == null || targetNameSnapshot.isBlank()) {
            return "(이름 없음)";
        }
        return targetNameSnapshot;
    }

    public boolean isCommunityTarget() {
        return communityCode != null && !communityCode.isBlank();
    }

    public String getDisplayCommunityName() {
        if (communityName == null || communityName.isBlank()) {
            return "전역";
        }
        return communityName;
    }

    public boolean isGlobalTarget() {
        return communityName == null || communityName.isBlank() || "전역".equals(communityName);
    }
}