package com.gahih.domain.report.repository;

import com.gahih.domain.report.dto.AdminReporterActivityResponse;
import com.gahih.domain.report.dto.AdminReporterActivitySearchCondition;
import com.gahih.domain.report.enumtype.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepositoryCustom {

    Page<AdminReporterActivityResponse> searchAdminReporterActivityPage(
            AdminReporterActivitySearchCondition condition,
            Pageable pageable
    );

    String summarizePendingReportCommunities(
            ReportTargetType targetType,
            Long targetId,
            Integer reportCycle
    );
}