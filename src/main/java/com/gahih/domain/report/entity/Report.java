package com.gahih.domain.report.entity;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.report.enumtype.ReportReasonType;
import com.gahih.domain.report.enumtype.ReportStatus;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "report_cycle", nullable = false)
    private Integer reportCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 50)
    private ReportReasonType reasonType;

    @Lob
    private String detail;

    @Column(length = 20)
    private String communityCode;

    @Column(length = 100)
    private String communityName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime handledAt;

    private Report(
            Member reporter,
            ReportTargetType targetType,
            Long targetId,
            Integer reportCycle,
            ReportReasonType reasonType,
            String detail,
            String communityCode,
            String communityName
    ) {
        validateReporter(reporter);
        validateTargetType(targetType);
        validateTargetId(targetId);
        validateReportCycle(reportCycle);
        validateReasonType(reasonType);

        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reportCycle = reportCycle;
        this.reasonType = reasonType;
        this.detail = normalizeDetail(detail);
        this.communityCode = normalizeShortText(communityCode);
        this.communityName = normalizeShortText(communityName);
        this.status = ReportStatus.PENDING;
    }

    public static Report create(
            Member reporter,
            ReportTargetType targetType,
            Long targetId,
            Integer reportCycle,
            ReportReasonType reasonType,
            String detail,
            String communityCode,
            String communityName
    ) {
        return new Report(reporter, targetType, targetId, reportCycle, reasonType, detail, communityCode, communityName);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
    }

    public void markHandled(ReportStatus handledStatus) {
        if (handledStatus != ReportStatus.ACTION_TAKEN && handledStatus != ReportStatus.NO_ACTION_NEEDED) {
            throw new DomainValidationException("신고 처리 상태는 ACTION_TAKEN 또는 NO_ACTION_NEEDED 여야 합니다.");
        }

        if (this.status != ReportStatus.PENDING) {
            throw new DomainValidationException("조치 전 상태의 신고만 처리할 수 있습니다.");
        }

        this.status = handledStatus;
        this.handledAt = LocalDateTime.now();
    }

    private void validateReporter(Member reporter) {
        if (reporter == null) {
            throw new DomainValidationException("신고자는 필수입니다.");
        }
    }

    private void validateTargetType(ReportTargetType targetType) {
        if (targetType == null) {
            throw new DomainValidationException("신고 대상 타입은 필수입니다.");
        }
    }

    private void validateTargetId(Long targetId) {
        if (targetId == null || targetId < 1) {
            throw new DomainValidationException("신고 대상 ID는 1 이상이어야 합니다.");
        }
    }

    private void validateReportCycle(Integer reportCycle) {
        if (reportCycle == null || reportCycle < 1) {
            throw new DomainValidationException("신고 회차는 1 이상이어야 합니다.");
        }
    }

    private void validateReasonType(ReportReasonType reasonType) {
        if (reasonType == null) {
            throw new DomainValidationException("신고 사유 타입은 필수입니다.");
        }
    }

    private String normalizeDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return null;
        }
        return detail.trim();
    }

    private String normalizeShortText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}