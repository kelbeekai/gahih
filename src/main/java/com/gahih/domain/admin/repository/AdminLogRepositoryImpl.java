package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.dto.AdminLogResponse;
import com.gahih.domain.admin.dto.AdminLogSearchCondition;
import com.gahih.domain.admin.entity.QAdminLog;
import com.gahih.domain.admin.enumtype.AdminLogSearchType;
import com.gahih.domain.admin.enumtype.AdminLogSortType;
import com.gahih.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class AdminLogRepositoryImpl implements AdminLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminLogResponse> searchLogPage(AdminLogSearchCondition condition, Pageable pageable) {
        QAdminLog adminLog = QAdminLog.adminLog;
        QMember admin = new QMember("admin");

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getActionOrNull() != null) {
            builder.and(adminLog.action.eq(condition.getActionOrNull()));
        }

        String keyword = condition.getKeywordOrNull();
        if (keyword != null) {
            builder.and(buildKeywordCondition(adminLog, admin, condition.getSearchType(), keyword));
        }

        if (condition.getTargetTypeOrNull() != null) {
            builder.and(adminLog.targetType.eq(condition.getTargetTypeOrNull()));
        }

        if (condition.getTargetCommunityCodeOrNull() != null) {
            builder.and(adminLog.targetCommunityCode.eq(condition.getTargetCommunityCodeOrNull()));
        }

        LocalDateTime startDateTime = condition.getStartDateTimeOrNull();
        if (startDateTime != null) {
            builder.and(adminLog.createdAt.goe(startDateTime));
        }

        LocalDateTime endDateTimeExclusive = condition.getEndDateTimeExclusiveOrNull();
        if (endDateTimeExclusive != null) {
            builder.and(adminLog.createdAt.lt(endDateTimeExclusive));
        }

        List<AdminLogResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminLogResponse.class,
                        adminLog.id,
                        admin.username,
                        admin.nickname,
                        adminLog.action,
                        adminLog.targetType,
                        adminLog.targetId,
                        adminLog.targetName,
                        adminLog.targetCommunityCode,
                        adminLog.targetCommunityName,
                        adminLog.reason,
                        adminLog.beforeSnapshot,
                        adminLog.afterSnapshot,
                        adminLog.createdAt
                ))
                .from(adminLog)
                .join(adminLog.admin, admin)
                .where(builder)
                .orderBy(getOrderSpecifier(adminLog, condition.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(adminLog.count())
                .from(adminLog)
                .join(adminLog.admin, admin)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public List<String> findDistinctActions() {
        QAdminLog adminLog = QAdminLog.adminLog;

        return queryFactory
                .select(adminLog.action)
                .from(adminLog)
                .distinct()
                .orderBy(adminLog.action.asc())
                .fetch();
    }

    private BooleanBuilder buildKeywordCondition(
            QAdminLog adminLog,
            QMember admin,
            AdminLogSearchType searchType,
            String keyword
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchType == null || searchType == AdminLogSearchType.ALL) {
            builder.or(admin.username.containsIgnoreCase(keyword));
            builder.or(admin.nickname.containsIgnoreCase(keyword));
            builder.or(adminLog.action.containsIgnoreCase(keyword));
            builder.or(adminLog.targetName.containsIgnoreCase(keyword));
            builder.or(adminLog.reason.like("%" + keyword + "%"));

            if (isNumeric(keyword)) {
                builder.or(adminLog.targetId.eq(Long.valueOf(keyword)));
            }

            return builder;
        }

        switch (searchType) {
            case ADMIN_USERNAME -> builder.and(admin.username.containsIgnoreCase(keyword));
            case ADMIN_NICKNAME -> builder.and(admin.nickname.containsIgnoreCase(keyword));
            case ACTION -> builder.and(adminLog.action.containsIgnoreCase(keyword));
            case TARGET_NAME -> builder.and(adminLog.targetName.containsIgnoreCase(keyword));
            case REASON -> builder.and(adminLog.reason.like("%" + keyword + "%"));
            case TARGET_ID -> {
                if (isNumeric(keyword)) {
                    builder.and(adminLog.targetId.eq(Long.valueOf(keyword)));
                } else {
                    builder.and(adminLog.targetId.isNull());
                }
            }
            default -> {
                builder.or(admin.username.containsIgnoreCase(keyword));
                builder.or(admin.nickname.containsIgnoreCase(keyword));
                builder.or(adminLog.action.containsIgnoreCase(keyword));
                builder.or(adminLog.targetName.containsIgnoreCase(keyword));
                builder.or(adminLog.reason.like("%" + keyword + "%"));

                if (isNumeric(keyword)) {
                    builder.or(adminLog.targetId.eq(Long.valueOf(keyword)));
                }
            }
        }

        return builder;
    }

    private OrderSpecifier<?> getOrderSpecifier(QAdminLog adminLog, AdminLogSortType sort) {
        if (sort == AdminLogSortType.OLDEST) {
            return adminLog.createdAt.asc();
        }
        return adminLog.createdAt.desc();
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}