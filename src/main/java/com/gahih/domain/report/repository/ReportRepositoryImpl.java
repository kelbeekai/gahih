package com.gahih.domain.report.repository;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.entity.QMember;
import com.gahih.domain.report.dto.AdminReporterActivityResponse;
import com.gahih.domain.report.dto.AdminReporterActivitySearchCondition;
import com.gahih.domain.report.entity.QReport;
import com.gahih.domain.report.enumtype.AdminReporterActivitySearchType;
import com.gahih.domain.report.enumtype.AdminReporterActivitySortType;
import com.gahih.domain.report.enumtype.ReportStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ReportRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AdminReporterActivityResponse> searchAdminReporterActivityPage(
            AdminReporterActivitySearchCondition condition,
            Pageable pageable
    ) {
        QReport report = QReport.report;
        QMember reporter = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        String communityCode = condition.getCommunityCodeOrNull();
        if (communityCode != null) {
            builder.and(report.communityCode.eq(communityCode));
        }

        applySearchCondition(builder, reporter, condition);

        NumberExpression<Long> totalReportCountExpr = report.id.count();

        NumberExpression<Integer> pendingCountExpr = Expressions.numberTemplate(
                Integer.class,
                "sum(case when {0} = {1} then 1 else 0 end)",
                report.status.stringValue(),
                Expressions.constant(ReportStatus.PENDING.name())
        );

        NumberExpression<Integer> actionTakenCountExpr = Expressions.numberTemplate(
                Integer.class,
                "sum(case when {0} = {1} then 1 else 0 end)",
                report.status.stringValue(),
                Expressions.constant(ReportStatus.ACTION_TAKEN.name())
        );

        NumberExpression<Integer> noActionNeededCountExpr = Expressions.numberTemplate(
                Integer.class,
                "sum(case when {0} = {1} then 1 else 0 end)",
                report.status.stringValue(),
                Expressions.constant(ReportStatus.NO_ACTION_NEEDED.name())
        );

        List<Tuple> aggregateRows = queryFactory
                .select(
                        reporter.id,
                        totalReportCountExpr,
                        pendingCountExpr,
                        actionTakenCountExpr,
                        noActionNeededCountExpr,
                        report.createdAt.max()
                )
                .from(report)
                .join(report.reporter, reporter)
                .where(builder)
                .groupBy(reporter.id)
                .orderBy(getOrderSpecifiers(
                        condition,
                        totalReportCountExpr,
                        pendingCountExpr,
                        actionTakenCountExpr,
                        noActionNeededCountExpr,
                        report
                ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> reporterIds = aggregateRows.stream()
                .map(row -> row.get(reporter.id))
                .toList();

        Map<Long, CommunityActivitySummary> communitySummaryMap =
                findCommunityActivitySummaryMap(reporterIds, communityCode);

        Map<Long, Member> memberMap = new HashMap<>();
        if (!reporterIds.isEmpty()) {
            List<Member> members = queryFactory
                    .selectFrom(reporter)
                    .where(reporter.id.in(reporterIds))
                    .fetch();

            for (Member member : members) {
                memberMap.put(member.getId(), member);
            }
        }

        List<AdminReporterActivityResponse> content = aggregateRows.stream()
                .map(row -> {
                    Long reporterId = row.get(reporter.id);
                    Member member = memberMap.get(reporterId);

                    return new AdminReporterActivityResponse(
                            reporterId,
                            member == null ? null : member.getUsername(),
                            member == null ? null : member.getNickname(),
                            member == null ? null : member.getRole().name(),
                            member == null ? null : member.getStatus().name(),
                            toLong(row.get(totalReportCountExpr)),
                            toLong(row.get(pendingCountExpr)),
                            toLong(row.get(actionTakenCountExpr)),
                            toLong(row.get(noActionNeededCountExpr)),
                            communitySummaryMap.getOrDefault(reporterId, CommunityActivitySummary.empty()).mainCommunityName(),
                            communitySummaryMap.getOrDefault(reporterId, CommunityActivitySummary.empty()).communitySummary(),
                            row.get(report.createdAt.max())
                    );
                })
                .toList();

        Long total = queryFactory
                .select(reporter.id.countDistinct())
                .from(report)
                .join(report.reporter, reporter)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private void applySearchCondition(
            BooleanBuilder builder,
            QMember reporter,
            AdminReporterActivitySearchCondition condition
    ) {
        String keyword = condition.getKeywordOrNull();
        if (keyword == null) {
            return;
        }

        AdminReporterActivitySearchType searchType = condition.getSearchType() == null
                ? AdminReporterActivitySearchType.ALL
                : condition.getSearchType();

        String trimmed = keyword.trim();
        String lowerKeyword = trimmed.toLowerCase();

        switch (searchType) {
            case MEMBER_ID -> builder.and(reporter.id.stringValue().contains(trimmed));
            case USERNAME -> builder.and(reporter.username.lower().contains(lowerKeyword));
            case NICKNAME -> builder.and(reporter.nickname.lower().contains(lowerKeyword));
            case ALL -> builder.and(
                    reporter.id.stringValue().contains(trimmed)
                            .or(reporter.username.lower().contains(lowerKeyword))
                            .or(reporter.nickname.lower().contains(lowerKeyword))
            );
        }
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(
            AdminReporterActivitySearchCondition condition,
            NumberExpression<Long> totalReportCountExpr,
            NumberExpression<Integer> pendingCountExpr,
            NumberExpression<Integer> actionTakenCountExpr,
            NumberExpression<Integer> noActionNeededCountExpr,
            QReport report
    ) {
        AdminReporterActivitySortType sortType = condition.getSort() == null
                ? AdminReporterActivitySortType.TOTAL_REPORT_DESC
                : condition.getSort();

        return switch (sortType) {
            case PENDING_REPORT_DESC -> new OrderSpecifier[]{
                    pendingCountExpr.desc(),
                    report.createdAt.max().desc()
            };
            case ACTION_TAKEN_DESC -> new OrderSpecifier[]{
                    actionTakenCountExpr.desc(),
                    report.createdAt.max().desc()
            };
            case NO_ACTION_NEEDED_DESC -> new OrderSpecifier[]{
                    noActionNeededCountExpr.desc(),
                    report.createdAt.max().desc()
            };
            case LATEST_REPORT_DESC -> new OrderSpecifier[]{
                    report.createdAt.max().desc()
            };
            case TOTAL_REPORT_DESC -> new OrderSpecifier[]{
                    totalReportCountExpr.desc(),
                    report.createdAt.max().desc()
            };
        };
    }

    private Map<Long, CommunityActivitySummary> findCommunityActivitySummaryMap(
            List<Long> reporterIds,
            String communityCode
    ) {
        if (reporterIds == null || reporterIds.isEmpty()) {
            return Map.of();
        }

        QReport report = QReport.report;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(report.reporter.id.in(reporterIds));
        builder.and(report.communityName.isNotNull());

        if (communityCode != null) {
            builder.and(report.communityCode.eq(communityCode));
        }

        List<Tuple> rows = queryFactory
                .select(
                        report.reporter.id,
                        report.communityName,
                        report.id.count(),
                        report.createdAt.max()
                )
                .from(report)
                .where(builder)
                .groupBy(report.reporter.id, report.communityName)
                .orderBy(
                        report.reporter.id.asc(),
                        report.id.count().desc(),
                        report.createdAt.max().desc()
                )
                .fetch();

        Map<Long, List<CommunityCount>> grouped = new HashMap<>();

        for (Tuple row : rows) {
            Long reporterId = row.get(report.reporter.id);
            String communityName = row.get(report.communityName);
            Long count = toLong(row.get(report.id.count()));

            if (reporterId == null || communityName == null || communityName.isBlank()) {
                continue;
            }

            grouped.computeIfAbsent(reporterId, key -> new java.util.ArrayList<>())
                    .add(new CommunityCount(communityName, count));
        }

        Map<Long, CommunityActivitySummary> result = new HashMap<>();

        for (Long reporterId : reporterIds) {
            List<CommunityCount> counts = grouped.get(reporterId);

            if (counts == null || counts.isEmpty()) {
                result.put(reporterId, CommunityActivitySummary.empty());
                continue;
            }

            String mainCommunityName = counts.get(0).communityName();

            String communitySummary = counts.stream()
                    .map(count -> count.communityName() + " " + count.count() + "건")
                    .toList()
                    .toString()
                    .replace("[", "")
                    .replace("]", "");

            result.put(reporterId, new CommunityActivitySummary(mainCommunityName, communitySummary));
        }

        return result;
    }

    private record CommunityCount(
            String communityName,
            Long count
    ) {
    }

    private record CommunityActivitySummary(
            String mainCommunityName,
            String communitySummary
    ) {
        private static CommunityActivitySummary empty() {
            return new CommunityActivitySummary("전역", "전역");
        }
    }

    @Override
    public String summarizePendingReportCommunities(
            com.gahih.domain.report.enumtype.ReportTargetType targetType,
            Long targetId,
            Integer reportCycle
    ) {
        QReport report = QReport.report;

        List<Tuple> rows = queryFactory
                .select(
                        report.communityName,
                        report.id.count()
                )
                .from(report)
                .where(
                        report.targetType.eq(targetType),
                        report.targetId.eq(targetId),
                        report.reportCycle.eq(reportCycle),
                        report.status.eq(ReportStatus.PENDING),
                        report.communityName.isNotNull()
                )
                .groupBy(report.communityName)
                .orderBy(report.id.count().desc(), report.communityName.asc())
                .fetch();

        if (rows.isEmpty()) {
            return "전역 또는 미확인";
        }

        return rows.stream()
                .map(row -> row.get(report.communityName) + " " + toLong(row.get(report.id.count())) + "건")
                .toList()
                .toString()
                .replace("[", "")
                .replace("]", "");
    }

    private Long toLong(Number value) {
        return value == null ? 0L : value.longValue();
    }
}