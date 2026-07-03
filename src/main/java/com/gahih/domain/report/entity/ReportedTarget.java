package com.gahih.domain.report.entity;

import com.gahih.domain.report.enumtype.ReportStatus;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reported_target",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reported_target_type_id",
                        columnNames = {"target_type", "target_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportedTarget {

    private static final int INITIAL_CYCLE = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status;

    @Column(nullable = false)
    private Integer currentCycle;

    @Column(nullable = false)
    private Long totalReportCount;

    @Column(nullable = false)
    private Long pendingReportCount;

    @Column(length = 255)
    private String targetNameSnapshot;

    @Column(length = 100)
    private String writerNicknameSnapshot;

    private Long writerMemberId;

    private Long parentPostId;

    @Column(length = 255)
    private String parentPostTitleSnapshot;

    @Column(length = 20)
    private String communityCode;

    @Column(length = 100)
    private String communityName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastReportedAt;
    private LocalDateTime lastHandledAt;

    private Long lastHandledByMemberId;

    @Lob
    private String lastDecisionReason;

    private ReportedTarget(
            ReportTargetType targetType,
            Long targetId,
            String targetNameSnapshot,
            Long writerMemberId,
            String writerNicknameSnapshot,
            Long parentPostId,
            String parentPostTitleSnapshot,
            String communityCode,
            String communityName
    ) {
        validateTargetType(targetType);
        validateTargetId(targetId);

        this.targetType = targetType;
        this.targetId = targetId;
        this.status = ReportStatus.PENDING;
        this.currentCycle = INITIAL_CYCLE;
        this.totalReportCount = 1L;
        this.pendingReportCount = 1L;
        this.targetNameSnapshot = normalizeShortText(targetNameSnapshot);
        this.writerMemberId = writerMemberId;
        this.writerNicknameSnapshot = normalizeShortText(writerNicknameSnapshot);
        this.parentPostId = parentPostId;
        this.parentPostTitleSnapshot = normalizeShortText(parentPostTitleSnapshot);
        this.lastReportedAt = LocalDateTime.now();
        this.communityCode = normalizeShortText(communityCode);
        this.communityName = normalizeShortText(communityName);
    }

    public static ReportedTarget createFirstReport(
            ReportTargetType targetType,
            Long targetId,
            String targetNameSnapshot,
            Long writerMemberId,
            String writerNicknameSnapshot,
            Long parentPostId,
            String parentPostTitleSnapshot,
            String communityCode,
            String communityName
    ) {
        return new ReportedTarget(
                targetType,
                targetId,
                targetNameSnapshot,
                writerMemberId,
                writerNicknameSnapshot,
                parentPostId,
                parentPostTitleSnapshot,
                communityCode,
                communityName
        );
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
        if (this.currentCycle == null) {
            this.currentCycle = INITIAL_CYCLE;
        }
        if (this.totalReportCount == null) {
            this.totalReportCount = 1L;
        }
        if (this.pendingReportCount == null) {
            this.pendingReportCount = 1L;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void applyNewReport(
            String targetNameSnapshot,
            Long writerMemberId,
            String writerNicknameSnapshot,
            Long parentPostId,
            String parentPostTitleSnapshot,
            String communityCode,
            String communityName
    ) {
        this.totalReportCount++;
        this.pendingReportCount++;
        this.status = ReportStatus.PENDING;
        this.lastReportedAt = LocalDateTime.now();

        this.targetNameSnapshot = normalizeShortText(targetNameSnapshot);
        this.writerMemberId = writerMemberId;
        this.writerNicknameSnapshot = normalizeShortText(writerNicknameSnapshot);
        this.parentPostId = parentPostId;
        this.parentPostTitleSnapshot = normalizeShortText(parentPostTitleSnapshot);

        this.communityCode = normalizeShortText(communityCode);
        this.communityName = normalizeShortText(communityName);
    }

    public void markHandled(ReportStatus handledStatus, Long adminMemberId, String decisionReason) {
        if (handledStatus != ReportStatus.ACTION_TAKEN && handledStatus != ReportStatus.NO_ACTION_NEEDED) {
            throw new DomainValidationException("신고 처리 상태는 ACTION_TAKEN 또는 NO_ACTION_NEEDED 여야 합니다.");
        }

        if (this.pendingReportCount == null || this.pendingReportCount < 1) {
            throw new DomainValidationException("처리할 미조치 신고가 없습니다.");
        }

        if (adminMemberId == null || adminMemberId < 1) {
            throw new DomainValidationException("처리 관리자 ID는 1 이상이어야 합니다.");
        }

        this.status = handledStatus;
        this.pendingReportCount = 0L;
        this.lastHandledAt = LocalDateTime.now();
        this.lastHandledByMemberId = adminMemberId;
        this.lastDecisionReason = normalizeLongText(decisionReason);
        this.currentCycle++;
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

    private String normalizeShortText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeLongText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public void anonymizeTargetSnapshot() {
        this.targetNameSnapshot = ANONYMIZED_TEXT;
    }

    public void anonymizeWriterSnapshot() {
        this.writerNicknameSnapshot = ANONYMIZED_TEXT;
    }

    private static final String ANONYMIZED_TEXT = "비식별 처리됨";
}