package com.gahih.domain.member.repository;

import com.gahih.domain.admin.dto.AdminMemberResponse;
import com.gahih.domain.admin.enumtype.AdminMemberSearchType;
import com.gahih.domain.admin.enumtype.AdminMemberSortType;
import com.gahih.domain.member.entity.QMember;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.report.entity.QReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminMemberResponse> searchAdminMemberPage(
            String keyword,
            AdminMemberSearchType searchType,
            MemberRole role,
            MemberStatus status,
            String sort,
            Pageable pageable
    ) {
        QMember member = QMember.member;

        QReportedTarget reportedTarget = QReportedTarget.reportedTarget;

        var totalReportCountExpr = JPAExpressions
                .select(reportedTarget.totalReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.MEMBER),
                        reportedTarget.targetId.eq(member.id)
                );

        var pendingReportCountExpr = JPAExpressions
                .select(reportedTarget.pendingReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.MEMBER),
                        reportedTarget.targetId.eq(member.id)
                );

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(keywordCondition(member, keyword, searchType));
        builder.and(roleEq(member, role));
        builder.and(statusEq(member, status));

        List<AdminMemberResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminMemberResponse.class,
                        member.id,
                        member.username,
                        member.nickname,
                        member.email,
                        member.role.stringValue(),
                        member.status.stringValue(),
                        member.createdAt,
                        totalReportCountExpr,
                        pendingReportCountExpr,
                        member.suspendedAt,
                        member.suspendedUntil,
                        member.suspensionReason,
                        member.suspensionCount
                ))
                .from(member)
                .where(builder)
                .orderBy(createOrderSpecifiers(member, sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private BooleanExpression keywordCondition(QMember member, String keyword, AdminMemberSearchType searchType) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        AdminMemberSearchType safeSearchType = searchType == null ? AdminMemberSearchType.ALL : searchType;

        return switch (safeSearchType) {
            case USERNAME -> member.username.containsIgnoreCase(keyword);
            case NICKNAME -> member.nickname.containsIgnoreCase(keyword);
            case EMAIL -> member.email.containsIgnoreCase(keyword);
            case ROLE -> member.role.stringValue().containsIgnoreCase(keyword);
            case STATUS -> member.status.stringValue().containsIgnoreCase(keyword);
            case ALL -> member.username.containsIgnoreCase(keyword)
                    .or(member.nickname.containsIgnoreCase(keyword))
                    .or(member.email.containsIgnoreCase(keyword))
                    .or(member.role.stringValue().containsIgnoreCase(keyword))
                    .or(member.status.stringValue().containsIgnoreCase(keyword));
        };
    }

    private BooleanExpression roleEq(QMember member, MemberRole role) {
        if (role == null) {
            return null;
        }
        return member.role.eq(role);
    }

    private BooleanExpression statusEq(QMember member, MemberStatus status) {
        if (status == null) {
            return null;
        }
        return member.status.eq(status);
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(QMember member, String sort) {
        AdminMemberSortType sortType = parseSort(sort);

        return switch (sortType) {
            case OLDEST -> new OrderSpecifier[]{
                    member.createdAt.asc(),
                    member.id.asc()
            };
            case NICKNAME -> new OrderSpecifier[]{
                    member.nickname.asc(),
                    member.id.desc()
            };
            case LATEST -> new OrderSpecifier[]{
                    member.createdAt.desc(),
                    member.id.desc()
            };
        };
    }

    private AdminMemberSortType parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return AdminMemberSortType.LATEST;
        }

        try {
            return AdminMemberSortType.valueOf(sort);
        } catch (IllegalArgumentException e) {
            return AdminMemberSortType.LATEST;
        }
    }
}
