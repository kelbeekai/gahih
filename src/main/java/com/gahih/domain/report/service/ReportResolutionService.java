package com.gahih.domain.report.service;

import com.gahih.domain.report.entity.Report;
import com.gahih.domain.report.entity.ReportedTarget;
import com.gahih.domain.report.enumtype.ReportStatus;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.repository.ReportRepository;
import com.gahih.domain.report.repository.ReportedTargetRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportResolutionService {

    private final ReportRepository reportRepository;
    private final ReportedTargetRepository reportedTargetRepository;

    public ReportedTarget getReportedTargetOrThrow(ReportTargetType targetType, Long targetId) {
        return reportedTargetRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .orElseThrow(() -> new NotFoundException("신고된 대상이 존재하지 않습니다."));
    }

    @Transactional
    public void resolveByActionIfPresent(
            ReportTargetType targetType,
            Long targetId,
            Long adminMemberId,
            String decisionReason
    ) {
        ReportedTarget reportedTarget = reportedTargetRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .orElse(null);

        if (reportedTarget == null || reportedTarget.getPendingReportCount() == 0L) {
            return;
        }

        applyResolution(reportedTarget, ReportStatus.ACTION_TAKEN, adminMemberId, decisionReason);
    }

    @Transactional
    public void markNoActionNeeded(
            ReportTargetType targetType,
            Long targetId,
            Long adminMemberId,
            String decisionReason
    ) {
        ReportedTarget reportedTarget = reportedTargetRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .orElseThrow(() -> new NotFoundException("신고된 대상이 존재하지 않습니다."));

        if (reportedTarget.getPendingReportCount() == 0L) {
            throw new BusinessException("처리할 미조치 신고가 없습니다.");
        }

        applyResolution(reportedTarget, ReportStatus.NO_ACTION_NEEDED, adminMemberId, decisionReason);
    }

    private void applyResolution(
            ReportedTarget reportedTarget,
            ReportStatus handledStatus,
            Long adminMemberId,
            String decisionReason
    ) {
        int handledCycle = reportedTarget.getCurrentCycle();

        List<Report> reports = reportRepository.findAllByTargetTypeAndTargetIdAndReportCycleAndStatus(
                reportedTarget.getTargetType(),
                reportedTarget.getTargetId(),
                handledCycle,
                ReportStatus.PENDING
        );

        if (reports.isEmpty()) {
            throw new BusinessException("처리할 미조치 신고가 없습니다.");
        }

        for (Report report : reports) {
            report.markHandled(handledStatus);
        }

        reportedTarget.markHandled(handledStatus, adminMemberId, decisionReason);
    }

}
