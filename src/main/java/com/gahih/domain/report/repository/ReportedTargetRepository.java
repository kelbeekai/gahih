package com.gahih.domain.report.repository;

import com.gahih.domain.report.entity.ReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportedTargetRepository extends JpaRepository<ReportedTarget, Long>, ReportedTargetRepositoryCustom  {

    Optional<ReportedTarget> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

    List<ReportedTarget> findAllByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

    List<ReportedTarget> findAllByWriterMemberId(Long writerMemberId);
}