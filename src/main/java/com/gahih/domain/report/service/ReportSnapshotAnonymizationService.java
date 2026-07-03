package com.gahih.domain.report.service;

import com.gahih.domain.report.entity.ReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.repository.ReportedTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportSnapshotAnonymizationService {

    private static final String ANONYMIZED_TEXT = "비식별 처리됨";

    private final ReportedTargetRepository reportedTargetRepository;

    public void anonymizeByFinalizedMember(Long memberId) {
        reportedTargetRepository.findAllByTargetTypeAndTargetId(ReportTargetType.MEMBER, memberId)
                .forEach(ReportedTarget::anonymizeTargetSnapshot);

        reportedTargetRepository.findAllByWriterMemberId(memberId)
                .forEach(ReportedTarget::anonymizeWriterSnapshot);
    }
}