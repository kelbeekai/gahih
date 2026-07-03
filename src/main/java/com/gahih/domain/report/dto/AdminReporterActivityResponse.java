package com.gahih.domain.report.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminReporterActivityResponse {

    private final Long reporterId;
    private final String username;
    private final String nickname;
    private final String role;
    private final String status;
    private final Long totalReportCount;
    private final Long pendingReportCount;
    private final Long actionTakenCount;
    private final Long noActionNeededCount;
    private final String mainCommunityName;
    private final String communitySummary;
    private final LocalDateTime latestReportAt;

    public AdminReporterActivityResponse(
            Long reporterId,
            String username,
            String nickname,
            String role,
            String status,
            Long totalReportCount,
            Long pendingReportCount,
            Long actionTakenCount,
            Long noActionNeededCount,
            String mainCommunityName,
            String communitySummary,
            LocalDateTime latestReportAt
    ) {
        this.reporterId = reporterId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
        this.totalReportCount = totalReportCount == null ? 0L : totalReportCount;
        this.pendingReportCount = pendingReportCount == null ? 0L : pendingReportCount;
        this.actionTakenCount = actionTakenCount == null ? 0L : actionTakenCount;
        this.noActionNeededCount = noActionNeededCount == null ? 0L : noActionNeededCount;
        this.mainCommunityName = (mainCommunityName == null || mainCommunityName.isBlank()) ? "전역" : mainCommunityName;
        this.communitySummary = (communitySummary == null || communitySummary.isBlank()) ? "전역" : communitySummary;
        this.latestReportAt = latestReportAt;
    }

    public String getSummary() {
        return pendingReportCount + "/" + totalReportCount;
    }

    public String getDisplayUsername() {
        if (isDeletedMember()) {
            return "비식별 처리됨";
        }

        if (username == null || username.isBlank()) {
            return "(아이디 없음)";
        }
        return username;
    }

    public String getDisplayNickname() {
        if (isDeletedMember()) {
            return "비식별 처리됨";
        }

        if (nickname == null || nickname.isBlank()) {
            return "(닉네임 없음)";
        }
        return nickname;
    }

    public String getDisplayStatus() {
        if (isDeletedMember()) {
            return "DELETED";
        }
        return status;
    }

    private boolean isDeletedMember() {
        return "DELETED".equalsIgnoreCase(status);
    }

    public String getActionTakenRate() {
        return formatRate(actionTakenCount, totalReportCount);
    }

    public String getNoActionNeededRate() {
        return formatRate(noActionNeededCount, totalReportCount);
    }

    private String formatRate(Long part, Long total) {
        long safePart = part == null ? 0L : part;
        long safeTotal = total == null ? 0L : total;

        if (safeTotal == 0L) {
            return "0.0%";
        }

        double rate = (safePart * 100.0) / safeTotal;
        return String.format("%.1f%%", rate);
    }
}