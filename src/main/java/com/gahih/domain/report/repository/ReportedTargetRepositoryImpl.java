package com.gahih.domain.report.repository;

import com.gahih.domain.report.dto.AdminReportResponse;
import com.gahih.domain.report.dto.AdminReportSearchCondition;
import com.gahih.domain.report.entity.QReport;
import com.gahih.domain.report.entity.QReportedTarget;
import com.gahih.domain.report.enumtype.AdminReportSearchType;
import com.gahih.domain.report.enumtype.AdminReportSortType;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ReportedTargetRepositoryImpl implements ReportedTargetRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminReportResponse> searchAdminReportPage(
            AdminReportSearchCondition condition,
            Pageable pageable
    ) {
        QReportedTarget reportedTarget = QReportedTarget.reportedTarget;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getCommunityCode() != null && !condition.getCommunityCode().isBlank()) {
            String communityCode = condition.getCommunityCode().toUpperCase();
            QReport report = QReport.report;

            builder.and(
                    reportedTarget.communityCode.eq(communityCode)
                            .or(
                                    reportedTarget.targetType.eq(ReportTargetType.MEMBER)
                                            .and(
                                                    JPAExpressions
                                                            .selectOne()
                                                            .from(report)
                                                            .where(
                                                                    report.targetType.eq(ReportTargetType.MEMBER),
                                                                    report.targetId.eq(reportedTarget.targetId),
                                                                    report.communityCode.eq(communityCode)
                                                            )
                                                            .exists()
                                            )
                            )
            );
        }

        if (condition.getTargetType() != null) {
            builder.and(reportedTarget.targetType.eq(condition.getTargetType()));
        }

        if (condition.getStatus() != null) {
            builder.and(reportedTarget.status.eq(condition.getStatus()));
        }

        String keyword = condition.getKeywordOrNull();
        if (keyword != null) {
            AdminReportSearchType searchType = condition.getSearchType() == null
                    ? AdminReportSearchType.TARGET_NAME
                    : condition.getSearchType();

            switch (searchType) {
                case TARGET_NAME -> builder.and(reportedTarget.targetNameSnapshot.containsIgnoreCase(keyword));
                case WRITER_NICKNAME -> builder.and(reportedTarget.writerNicknameSnapshot.containsIgnoreCase(keyword));
                case PARENT_POST_TITLE -> builder.and(reportedTarget.parentPostTitleSnapshot.containsIgnoreCase(keyword));
                case TARGET_ID -> {
                    try {
                        builder.and(reportedTarget.targetId.eq(Long.parseLong(keyword)));
                    } catch (NumberFormatException e) {
                        builder.and(reportedTarget.id.isNull());
                    }
                }
            }
        }

        List<Tuple> rows = queryFactory
                .select(
                        reportedTarget.id,
                        reportedTarget.targetType,
                        reportedTarget.targetId,
                        reportedTarget.status,
                        reportedTarget.currentCycle,
                        reportedTarget.totalReportCount,
                        reportedTarget.pendingReportCount,
                        reportedTarget.targetNameSnapshot,
                        reportedTarget.writerNicknameSnapshot,
                        reportedTarget.parentPostId,
                        reportedTarget.parentPostTitleSnapshot,
                        reportedTarget.communityCode,
                        reportedTarget.communityName,
                        reportedTarget.lastReportedAt,
                        reportedTarget.lastHandledAt,
                        reportedTarget.lastHandledByMemberId,
                        reportedTarget.lastDecisionReason
                )
                .from(reportedTarget)
                .where(builder)
                .orderBy(createOrderSpecifiers(condition, reportedTarget))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Map<Long, String> memberReportCommunitySummaryMap = findMemberReportCommunitySummaryMap(rows);

        List<AdminReportResponse> content = rows.stream()
                .map(row -> {
                    ReportTargetType targetType = row.get(reportedTarget.targetType);
                    Long targetId = row.get(reportedTarget.targetId);

                    String communityCode = row.get(reportedTarget.communityCode);
                    String communityName = row.get(reportedTarget.communityName);

                    if (targetType == ReportTargetType.MEMBER) {
                        communityName = memberReportCommunitySummaryMap.getOrDefault(targetId, "전역");
                        communityCode = null;
                    }

                    return new AdminReportResponse(
                            row.get(reportedTarget.id),
                            targetType,
                            targetId,
                            row.get(reportedTarget.status),
                            row.get(reportedTarget.currentCycle),
                            row.get(reportedTarget.totalReportCount),
                            row.get(reportedTarget.pendingReportCount),
                            row.get(reportedTarget.targetNameSnapshot),
                            row.get(reportedTarget.writerNicknameSnapshot),
                            row.get(reportedTarget.parentPostId),
                            row.get(reportedTarget.parentPostTitleSnapshot),
                            communityCode,
                            communityName,
                            row.get(reportedTarget.lastReportedAt),
                            row.get(reportedTarget.lastHandledAt),
                            row.get(reportedTarget.lastHandledByMemberId),
                            row.get(reportedTarget.lastDecisionReason)
                    );
                })
                .toList();

        Long total = queryFactory
                .select(reportedTarget.count())
                .from(reportedTarget)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(
            AdminReportSearchCondition condition,
            QReportedTarget reportedTarget
    ) {
        AdminReportSortType sortType = condition.getSort() == null
                ? AdminReportSortType.PENDING_OLDEST
                : condition.getSort();

        return switch (sortType) {
            case LAST_REPORTED_DESC -> new OrderSpecifier[]{
                    reportedTarget.lastReportedAt.desc(),
                    reportedTarget.id.desc()
            };
            case REPORT_COUNT_DESC -> new OrderSpecifier[]{
                    reportedTarget.pendingReportCount.desc(),
                    reportedTarget.totalReportCount.desc(),
                    reportedTarget.lastReportedAt.asc()
            };
            case PENDING_OLDEST -> new OrderSpecifier[]{
                    reportedTarget.lastReportedAt.asc(),
                    reportedTarget.id.asc()
            };
        };
    }

    private Map<Long, String> findMemberReportCommunitySummaryMap(List<Tuple> rows) {
        QReportedTarget reportedTarget = QReportedTarget.reportedTarget;

        List<Long> memberTargetIds = rows.stream()
                .filter(row -> row.get(reportedTarget.targetType) == ReportTargetType.MEMBER)
                .map(row -> row.get(reportedTarget.targetId))
                .distinct()
                .toList();

        if (memberTargetIds.isEmpty()) {
            return Map.of();
        }

        QReport report = QReport.report;

        List<Tuple> communityRows = queryFactory
                .select(
                        report.targetId,
                        report.communityName,
                        report.createdAt.min()
                )
                .from(report)
                .where(
                        report.targetType.eq(ReportTargetType.MEMBER),
                        report.targetId.in(memberTargetIds),
                        report.communityName.isNotNull()
                )
                .groupBy(report.targetId, report.communityName)
                .orderBy(report.targetId.asc(), report.createdAt.min().asc())
                .fetch();

        Map<Long, List<String>> communityNamesByTargetId = new HashMap<>();

        for (Tuple row : communityRows) {
            Long targetId = row.get(report.targetId);
            String communityName = row.get(report.communityName);

            if (targetId == null || communityName == null || communityName.isBlank()) {
                continue;
            }

            communityNamesByTargetId
                    .computeIfAbsent(targetId, key -> new java.util.ArrayList<>())
                    .add(communityName);
        }

        Map<Long, String> result = new HashMap<>();

        for (Long targetId : memberTargetIds) {
            List<String> names = communityNamesByTargetId.get(targetId);

            if (names == null || names.isEmpty()) {
                result.put(targetId, "전역");
                continue;
            }

            if (names.size() == 1) {
                result.put(targetId, names.get(0));
                continue;
            }

            result.put(targetId, names.get(0) + " 외 " + (names.size() - 1) + "개");
        }

        return result;
    }
}