package com.gahih.domain.admin.service;

import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.report.dto.AdminReportResponse;
import com.gahih.domain.report.dto.AdminReportSearchCondition;
import com.gahih.domain.report.dto.AdminReporterActivityResponse;
import com.gahih.domain.report.dto.AdminReporterActivitySearchCondition;
import com.gahih.domain.report.entity.ReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.service.ReportAdminService;
import com.gahih.domain.report.service.ReportResolutionService;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final MemberRepository memberRepository;
    private final AdminLogRepository adminLogRepository;
    private final ReportAdminService reportAdminService;
    private final ReportResolutionService resolutionService;

    public Page<AdminReportResponse> searchReports(AdminReportSearchCondition condition) {
        return reportAdminService.searchReportedTargets(condition);
    }

    public Page<AdminReporterActivityResponse> searchReporterActivities(
            AdminReporterActivitySearchCondition condition
    ) {
        return reportAdminService.searchReporterActivities(condition);
    }

    @Transactional
    public void markReportNoActionNeeded(
            Long adminMemberId,
            ReportTargetType targetType,
            Long targetId,
            String reason
    ) {
        Member admin = getAdmin(adminMemberId);
        String normalizedReason = normalizeReason(reason);

        var beforeTarget = resolutionService.getReportedTargetOrThrow(targetType, targetId);
        String targetName = resolveReportLogTargetName(beforeTarget);
        String beforeSnapshot = buildReportResolutionSnapshot(beforeTarget);

        resolutionService.markNoActionNeeded(targetType, targetId, admin.getId(), normalizedReason);

        var afterTarget = resolutionService.getReportedTargetOrThrow(targetType, targetId);
        String afterSnapshot = buildReportResolutionSnapshot(afterTarget);

        saveLog(
                admin,
                "REPORT_NO_ACTION_NEEDED",
                toAdminLogTargetType(targetType),
                targetId,
                targetName,
                beforeTarget.getCommunityCode(),
                beforeTarget.getCommunityName(),
                normalizedReason,
                beforeSnapshot,
                afterSnapshot
        );

    }

    private Member getAdmin(Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 관리자입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new UnauthorizedException("관리자 권한이 없습니다.");
        }

        return admin;
    }

    private void saveLog(
            Member admin,
            String action,
            AdminLogTargetType targetType,
            Long targetId,
            String targetName,
            String targetCommunityCode,
            String targetCommunityName,
            String reason,
            String beforeSnapshot,
            String afterSnapshot
    ) {
        AdminLog adminLog = AdminLog.create(
                admin,
                action,
                targetType,
                targetId,
                targetName,
                reason,
                beforeSnapshot,
                afterSnapshot,
                targetCommunityCode,
                targetCommunityName
        );
        adminLogRepository.save(adminLog);

        log.info(
                "Admin action completed. action={}, adminId={}, targetType={}, targetId={}, communityCode={}",
                action,
                admin.getId(),
                targetType,
                targetId,
                targetCommunityCode
        );
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }

    private AdminLogTargetType toAdminLogTargetType(ReportTargetType targetType) {
        return switch (targetType) {
            case MEMBER -> AdminLogTargetType.MEMBER;
            case POST -> AdminLogTargetType.POST;
            case COMMENT -> AdminLogTargetType.COMMENT;
            case ATTACHMENT -> AdminLogTargetType.ATTACHMENT;
        };
    }

    private String resolveReportLogTargetName(com.gahih.domain.report.entity.ReportedTarget reportedTarget) {
        if (reportedTarget.getTargetNameSnapshot() != null && !reportedTarget.getTargetNameSnapshot().isBlank()) {
            return reportedTarget.getTargetNameSnapshot();
        }

        return switch (reportedTarget.getTargetType()) {
            case MEMBER -> "회원";
            case POST -> "게시글";
            case COMMENT -> "댓글";
            case ATTACHMENT -> "첨부파일";
        };
    }

    private String buildReportResolutionSnapshot(ReportedTarget reportedTarget) {
        String reportCommunitySummary = reportAdminService.summarizePendingReportCommunities(
                reportedTarget.getTargetType(),
                reportedTarget.getTargetId(),
                reportedTarget.getCurrentCycle()
        );

        return "reportStatus=" + reportedTarget.getStatus().name()
                + ", currentCycle=" + reportedTarget.getCurrentCycle()
                + ", totalReportCount=" + reportedTarget.getTotalReportCount()
                + ", pendingReportCount=" + reportedTarget.getPendingReportCount()
                + ", reportCommunitySummary=" + reportCommunitySummary
                + ", targetNameSnapshot=" + safeSnapshotValue(reportedTarget.getTargetNameSnapshot())
                + ", writerNicknameSnapshot=" + safeSnapshotValue(reportedTarget.getWriterNicknameSnapshot())
                + ", parentPostId=" + reportedTarget.getParentPostId()
                + ", lastHandledByMemberId=" + reportedTarget.getLastHandledByMemberId()
                + ", lastDecisionReason=" + safeSnapshotValue(reportedTarget.getLastDecisionReason());
    }

    private String safeSnapshotValue(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}
