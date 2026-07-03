package com.gahih.domain.report.repository;

import com.gahih.domain.report.entity.Report;
import com.gahih.domain.report.enumtype.ReportStatus;
import com.gahih.domain.report.enumtype.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom {

    boolean existsByReporterIdAndTargetTypeAndTargetIdAndReportCycle(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId,
            Integer reportCycle
    );

    List<Report> findAllByTargetTypeAndTargetIdAndReportCycleAndStatus(
            ReportTargetType targetType,
            Long targetId,
            Integer reportCycle,
            ReportStatus status
    );
}