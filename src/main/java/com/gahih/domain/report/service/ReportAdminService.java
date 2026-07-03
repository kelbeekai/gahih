package com.gahih.domain.report.service;

import com.gahih.domain.report.dto.AdminReportResponse;
import com.gahih.domain.report.dto.AdminReportSearchCondition;
import com.gahih.domain.report.dto.AdminReporterActivityResponse;
import com.gahih.domain.report.dto.AdminReporterActivitySearchCondition;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.repository.ReportRepository;
import com.gahih.domain.report.repository.ReportedTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportAdminService {

    private final ReportRepository reportRepository;
    private final ReportedTargetRepository reportedTargetRepository;

    public Page<AdminReportResponse> searchReportedTargets(AdminReportSearchCondition condition) {
        return reportedTargetRepository.searchAdminReportPage(
                condition,
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public Page<AdminReporterActivityResponse> searchReporterActivities(
            AdminReporterActivitySearchCondition condition
    ) {
        return reportRepository.searchAdminReporterActivityPage(
                condition,
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public String summarizePendingReportCommunities(
            ReportTargetType targetType,
            Long targetId,
            Integer reportCycle
    ) {
        return reportRepository.summarizePendingReportCommunities(targetType, targetId, reportCycle);
    }
}
