package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.dto.AdminNicknameHistoryResponse;
import com.gahih.domain.admin.dto.AdminNicknameHistorySearchCondition;
import com.gahih.domain.admin.enumtype.AdminNicknameHistorySearchType;
import com.gahih.domain.member.entity.QMember;
import com.gahih.domain.member.entity.QNicknameHistory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminNicknameHistoryQueryRepositoryImpl implements AdminNicknameHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminNicknameHistoryResponse> searchNicknameHistoryPage(
            AdminNicknameHistorySearchCondition condition,
            Pageable pageable
    ) {
        QNicknameHistory nicknameHistory = QNicknameHistory.nicknameHistory;
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        String keyword = condition.getKeywordOrNull();
        if (keyword != null) {
            builder.and(buildKeywordCondition(nicknameHistory, member, condition.getSearchType(), keyword));
        }

        String changeType = condition.getChangeTypeOrNull();
        if (changeType != null) {
            if ("INITIAL".equals(changeType)) {
                builder.and(nicknameHistory.previousNickname.isNull())
                        .and(nicknameHistory.newNickname.isNotNull());
            } else if ("CHANGE".equals(changeType)) {
                builder.and(nicknameHistory.previousNickname.isNotNull())
                        .and(nicknameHistory.newNickname.isNotNull());
            }
        }

        List<AdminNicknameHistoryResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminNicknameHistoryResponse.class,
                        nicknameHistory.id,
                        member.id,
                        member.nickname,
                        member.status,
                        nicknameHistory.previousNickname,
                        nicknameHistory.newNickname,
                        nicknameHistory.changeType,
                        nicknameHistory.changedAt
                ))
                .from(nicknameHistory)
                .join(nicknameHistory.member, member)
                .where(builder)
                .orderBy("OLDEST".equals(condition.getSortName())
                        ? nicknameHistory.changedAt.asc()
                        : nicknameHistory.changedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(nicknameHistory.count())
                .from(nicknameHistory)
                .join(nicknameHistory.member, member)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildKeywordCondition(
            QNicknameHistory nicknameHistory,
            QMember member,
            AdminNicknameHistorySearchType searchType,
            String keyword
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchType == null || searchType == AdminNicknameHistorySearchType.ALL) {
            if (isNumeric(keyword)) {
                builder.or(member.id.eq(Long.valueOf(keyword)));
            }
            builder.or(member.nickname.containsIgnoreCase(keyword));
            builder.or(nicknameHistory.previousNickname.containsIgnoreCase(keyword));
            builder.or(nicknameHistory.newNickname.containsIgnoreCase(keyword));
            return builder;
        }

        switch (searchType) {
            case MEMBER_ID -> {
                if (isNumeric(keyword)) {
                    builder.and(member.id.eq(Long.valueOf(keyword)));
                } else {
                    builder.and(member.id.isNull());
                }
            }
            case CURRENT_NICKNAME -> builder.and(member.nickname.containsIgnoreCase(keyword));
            case PREVIOUS_NICKNAME -> builder.and(nicknameHistory.previousNickname.containsIgnoreCase(keyword));
            case NEW_NICKNAME -> builder.and(nicknameHistory.newNickname.containsIgnoreCase(keyword));
            default -> {
                if (isNumeric(keyword)) {
                    builder.or(member.id.eq(Long.valueOf(keyword)));
                }
                builder.or(member.nickname.containsIgnoreCase(keyword));
                builder.or(nicknameHistory.previousNickname.containsIgnoreCase(keyword));
                builder.or(nicknameHistory.newNickname.containsIgnoreCase(keyword));
            }
        }

        return builder;
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