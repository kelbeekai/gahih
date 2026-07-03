package com.gahih.domain.admin.dto;

import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminLogResponse {

    private final Long id;
    private final String adminUsername;
    private final String adminNickname;
    private final String action;
    private final AdminLogTargetType targetType;
    private final Long targetId;
    private final String targetName;
    private final String targetCommunityCode;
    private final String targetCommunityName;
    private final String reason;
    private final String beforeSnapshot;
    private final String afterSnapshot;
    private final LocalDateTime createdAt;

    public AdminLogResponse(
            Long id,
            String adminUsername,
            String adminNickname,
            String action,
            AdminLogTargetType targetType,
            Long targetId,
            String targetName,
            String targetCommunityCode,
            String targetCommunityName,
            String reason,
            String beforeSnapshot,
            String afterSnapshot,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.adminUsername = adminUsername;
        this.adminNickname = adminNickname;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetCommunityCode = targetCommunityCode;
        this.targetCommunityName = targetCommunityName;
        this.reason = reason;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        this.createdAt = createdAt;
    }

    public static AdminLogResponse from(AdminLog adminLog) {
        return new AdminLogResponse(
                adminLog.getId(),
                adminLog.getAdmin().getUsername(),
                adminLog.getAdmin().getNickname(),
                adminLog.getAction(),
                adminLog.getTargetType(),
                adminLog.getTargetId(),
                adminLog.getTargetName(),
                adminLog.getTargetCommunityCode(),
                adminLog.getTargetCommunityName(),
                adminLog.getReason(),
                adminLog.getBeforeSnapshot(),
                adminLog.getAfterSnapshot(),
                adminLog.getCreatedAt()
        );
    }

    public String getDisplayReason() {
        return (reason == null || reason.isBlank()) ? "-" : reason;
    }

    public String getDisplayBeforeSnapshot() {
        return (beforeSnapshot == null || beforeSnapshot.isBlank()) ? "-" : beforeSnapshot;
    }

    public String getDisplayAfterSnapshot() {
        return (afterSnapshot == null || afterSnapshot.isBlank()) ? "-" : afterSnapshot;
    }

    public boolean hasReason() {
        return reason != null && !reason.isBlank();
    }

    public boolean hasBeforeSnapshot() {
        return beforeSnapshot != null && !beforeSnapshot.isBlank();
    }

    public boolean hasAfterSnapshot() {
        return afterSnapshot != null && !afterSnapshot.isBlank();
    }

    public String getPrettyBeforeSnapshot() {
        return prettifySnapshot(beforeSnapshot);
    }

    public String getPrettyAfterSnapshot() {
        return prettifySnapshot(afterSnapshot);
    }

    private String prettifySnapshot(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) {
            return "-";
        }
        return snapshot.replace(", ", "\n");
    }

    public String getDisplayTargetCommunityName() {
        if (targetCommunityName == null || targetCommunityName.isBlank()) {
            return "전역";
        }
        return targetCommunityName;
    }
}