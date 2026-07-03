package com.gahih.domain.report.repository;

import com.gahih.domain.report.dto.AdminReportResponse;
import com.gahih.domain.report.dto.AdminReportSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportedTargetRepositoryCustom {

    Page<AdminReportResponse> searchAdminReportPage(
            AdminReportSearchCondition condition,
            Pageable pageable
    );
}