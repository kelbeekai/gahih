package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.dto.AdminNicknameReservationResponse;
import com.gahih.domain.admin.dto.AdminNicknameReservationSearchCondition;
import com.gahih.domain.admin.enumtype.AdminNicknameReservationSearchType;
import com.gahih.domain.member.entity.QNicknameReservation;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
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
public class AdminNicknameReservationQueryRepositoryImpl implements AdminNicknameReservationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminNicknameReservationResponse> searchNicknameReservationPage(
            AdminNicknameReservationSearchCondition condition,
            Pageable pageable
    ) {
        QNicknameReservation nicknameReservation = QNicknameReservation.nicknameReservation;

        BooleanBuilder builder = new BooleanBuilder();

        String keyword = condition.getKeywordOrNull();
        if (keyword != null) {
            builder.and(buildKeywordCondition(nicknameReservation, condition.getSearchType(), keyword));
        }

        String reasonType = condition.getReasonTypeOrNull();
        if (reasonType != null) {
            builder.and(nicknameReservation.reasonType.eq(reasonType));
        }

        List<AdminNicknameReservationResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminNicknameReservationResponse.class,
                        nicknameReservation.id,
                        nicknameReservation.nickname,
                        nicknameReservation.reasonType,
                        nicknameReservation.expiresAt,
                        nicknameReservation.createdAt
                ))
                .from(nicknameReservation)
                .where(builder)
                .orderBy(getOrderSpecifier(condition.getSortName(), nicknameReservation))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(nicknameReservation.count())
                .from(nicknameReservation)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildKeywordCondition(
            QNicknameReservation nicknameReservation,
            AdminNicknameReservationSearchType searchType,
            String keyword
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchType == null || searchType == AdminNicknameReservationSearchType.ALL) {
            builder.or(nicknameReservation.nickname.containsIgnoreCase(keyword));
            builder.or(nicknameReservation.reasonType.containsIgnoreCase(keyword));
            return builder;
        }

        switch (searchType) {
            case NICKNAME -> builder.and(nicknameReservation.nickname.containsIgnoreCase(keyword));
            case REASON_TYPE -> builder.and(nicknameReservation.reasonType.containsIgnoreCase(keyword));
            default -> {
                builder.or(nicknameReservation.nickname.containsIgnoreCase(keyword));
                builder.or(nicknameReservation.reasonType.containsIgnoreCase(keyword));
            }
        }

        return builder;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortName, QNicknameReservation nicknameReservation) {
        return switch (sortName) {
            case "LATEST" -> nicknameReservation.createdAt.desc();
            case "OLDEST" -> nicknameReservation.createdAt.asc();
            case "EXPIRES_SOON" -> nicknameReservation.expiresAt.asc();
            default -> nicknameReservation.expiresAt.asc();
        };
    }
}