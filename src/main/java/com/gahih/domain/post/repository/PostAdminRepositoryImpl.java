package com.gahih.domain.post.repository;

import com.gahih.domain.admin.dto.AdminPostResponse;
import com.gahih.domain.admin.enumtype.AdminPostSortType;
import com.gahih.domain.comment.entity.QComment;
import com.gahih.domain.post.entity.QPost;
import com.gahih.domain.post.entity.QPostAttachment;
import com.gahih.domain.post.entity.QPostTradeInfo;
import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.report.entity.QReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class PostAdminRepositoryImpl implements PostAdminRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminPostResponse> searchAdminPostPage(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            String sort,
            Pageable pageable
    ) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostAttachment attachment = QPostAttachment.postAttachment;
        QPost pinnedPost = new QPost("pinnedPost");

        QReportedTarget reportedTarget = QReportedTarget.reportedTarget;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;

        var totalReportCountExpr = JPAExpressions
                .select(reportedTarget.totalReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.POST),
                        reportedTarget.targetId.eq(post.id)
                );

        var pendingReportCountExpr = JPAExpressions
                .select(reportedTarget.pendingReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.POST),
                        reportedTarget.targetId.eq(post.id)
                );

        NumberExpression<Long> attachmentCount = attachment.id.count();
        NumberExpression<Long> attachmentDownloadSum = attachment.downloadCount.sumAggregate().coalesce(0L);

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(communityEq(communityCode));
        builder.and(categoryEq(categoryId));
        builder.and(keywordContains(keyword));
        builder.and(secretEq(secret));
        builder.and(onlyWithAttachments(onlyWithAttachments));
        builder.and(tradeTypeEq(tradeInfo, tradeType));
        builder.and(tradeStatusEq(tradeInfo, tradeStatus));

        var pinnedCountInCategory = JPAExpressions
                .select(pinnedPost.count())
                .from(pinnedPost)
                .where(
                        pinnedPost.category.id.eq(post.category.id),
                        pinnedPost.pinned.isTrue()
                );

        var pinAllowedExpr = new CaseBuilder()
                .when(post.pinned.isFalse().and(pinnedCountInCategory.lt(5L)))
                .then(true)
                .otherwise(false);

        List<AdminPostResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminPostResponse.class,
                        post.id,
                        post.title,
                        post.member.nickname,
                        post.member.status,
                        post.category.name,
                        post.category.countryCommunity.code,
                        post.category.countryCommunity.name,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        comment.id.countDistinct(),
                        post.createdAt,
                        post.editedAt,
                        attachmentCount,
                        attachmentDownloadSum,
                        post.zipDownloadCount,
                        post.pinned,
                        pinAllowedExpr,
                        post.pinnedAt,
                        post.secret,
                        post.status,
                        post.userDeletedBeforeAdminAction,
                        totalReportCountExpr,
                        pendingReportCountExpr,
                        tradeInfo.type,
                        tradeInfo.status
                ))
                .from(post)
                .leftJoin(comment).on(comment.post.id.eq(post.id))
                .leftJoin(post.attachments, attachment)
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .where(builder)
                .groupBy(
                        post.id,
                        post.title,
                        post.member.nickname,
                        post.member.status,
                        post.category.name,
                        post.category.countryCommunity.code,
                        post.category.countryCommunity.name,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        post.zipDownloadCount,
                        post.createdAt,
                        post.editedAt,
                        post.pinned,
                        post.pinnedAt,
                        post.secret,
                        post.status,
                        post.userDeletedBeforeAdminAction,
                        tradeInfo.type,
                        tradeInfo.status
                )
                .orderBy(createAdminOrderSpecifiers(sort, post, attachmentDownloadSum, comment.id.countDistinct()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.id.countDistinct())
                .from(post)
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public List<AdminPostResponse> findPinnedAdminPostList(
            String communityCode,
            Long categoryId,
            TradeType tradeType,
            TradeStatus tradeStatus,
            int limit
    ) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostAttachment attachment = QPostAttachment.postAttachment;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;

        QReportedTarget reportedTarget = QReportedTarget.reportedTarget;

        var totalReportCountExpr = JPAExpressions
                .select(reportedTarget.totalReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.POST),
                        reportedTarget.targetId.eq(post.id)
                );

        var pendingReportCountExpr = JPAExpressions
                .select(reportedTarget.pendingReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.POST),
                        reportedTarget.targetId.eq(post.id)
                );

        NumberExpression<Long> attachmentCount = attachment.id.count();
        NumberExpression<Long> attachmentDownloadSum = attachment.downloadCount.sumAggregate().coalesce(0L);

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.pinned.isTrue());
        builder.and(communityEq(communityCode));
        builder.and(categoryEq(categoryId));
        builder.and(post.status.eq(PostStatus.ACTIVE)); // ACTIVE 상태만 고정 가능
        builder.and(tradeTypeEq(tradeInfo, tradeType));
        builder.and(tradeStatusEq(tradeInfo, tradeStatus));

        var pinAllowedExpr = new CaseBuilder()
                .when(post.id.isNotNull())
                .then(false)
                .otherwise(false);

        return queryFactory
                .select(Projections.constructor(
                        AdminPostResponse.class,
                        post.id,
                        post.title,
                        post.member.nickname,
                        post.member.status,
                        post.category.name,
                        post.category.countryCommunity.code,
                        post.category.countryCommunity.name,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        comment.id.countDistinct(),
                        post.createdAt,
                        post.editedAt,
                        attachmentCount,
                        attachmentDownloadSum,
                        post.zipDownloadCount,
                        post.pinned,
                        pinAllowedExpr,
                        post.pinnedAt,
                        post.secret,
                        post.status,
                        post.userDeletedBeforeAdminAction,
                        totalReportCountExpr,
                        pendingReportCountExpr,
                        tradeInfo.type,
                        tradeInfo.status
                ))
                .from(post)
                .leftJoin(comment).on(comment.post.id.eq(post.id))
                .leftJoin(post.attachments, attachment)
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .where(builder)
                .groupBy(
                        post.id,
                        post.title,
                        post.member.nickname,
                        post.member.status,
                        post.category.name,
                        post.category.countryCommunity.code,
                        post.category.countryCommunity.name,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        post.createdAt,
                        post.editedAt,
                        post.zipDownloadCount,
                        post.pinned,
                        post.pinnedAt,
                        post.secret,
                        post.status,
                        post.userDeletedBeforeAdminAction,
                        tradeInfo.type,
                        tradeInfo.status
                )
                .orderBy(post.pinnedAt.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Long> findOrderedAdminPostIdsForNavigation(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            String sort
    ) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostAttachment attachment = QPostAttachment.postAttachment;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(communityEq(communityCode));
        builder.and(categoryEq(categoryId));
        builder.and(keywordContains(keyword));
        builder.and(secretEq(secret));
        builder.and(onlyWithAttachments(onlyWithAttachments));
        builder.and(tradeTypeEq(tradeInfo, tradeType));
        builder.and(tradeStatusEq(tradeInfo, tradeStatus));

        NumberExpression<Long> attachmentDownloadSum = attachment.downloadCount.sumAggregate().coalesce(0L);

        return queryFactory
                .select(post.id)
                .from(post)
                .leftJoin(comment).on(comment.post.id.eq(post.id))
                .leftJoin(post.attachments, attachment)
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .where(builder)
                .groupBy(
                        post.id,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        post.zipDownloadCount
                )
                .orderBy(createAdminOrderSpecifiers(sort, post, attachmentDownloadSum, comment.id.countDistinct()))
                .fetch();
    }

    private OrderSpecifier<?>[] createAdminOrderSpecifiers(
            String sort,
            QPost post,
            NumberExpression<Long> attachmentDownloadSum,
            NumberExpression<Long> commentCount
    ) {
        AdminPostSortType sortType = parseAdminSort(sort);

        return switch (sortType) {
            case OLDEST -> new OrderSpecifier[]{
                    post.id.asc()
            };
            case VIEWS -> new OrderSpecifier[]{
                    post.viewCount.desc(),
                    post.id.desc()
            };
            case ATTACHMENT_DOWNLOADS -> new OrderSpecifier[]{
                    attachmentDownloadSum.desc(),
                    post.id.desc()
            };
            case ZIP_DOWNLOADS -> new OrderSpecifier[]{
                    post.zipDownloadCount.desc(),
                    post.id.desc()
            };
            case LATEST -> new OrderSpecifier[]{
                    post.id.desc()
            };
            case LIKE_COUNT -> new OrderSpecifier[]{
                    post.likeCount.desc(),
                    post.id.desc()
            };
            case DISLIKE_COUNT -> new OrderSpecifier[]{
                    post.dislikeCount.desc(),
                    post.id.desc()
            };
            case COMMENT_COUNT -> new OrderSpecifier[]{
                    commentCount.desc(),
                    post.id.desc()
            };
        };
    }

    private AdminPostSortType parseAdminSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return AdminPostSortType.LATEST;
        }

        try {
            return AdminPostSortType.valueOf(sort);
        } catch (IllegalArgumentException e) {
            return AdminPostSortType.LATEST;
        }
    }

    private BooleanExpression categoryEq(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return QPost.post.category.id.eq(categoryId);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QPost.post.title.containsIgnoreCase(keyword)
                .or(QPost.post.member.nickname.containsIgnoreCase(keyword))
                .or(QPost.post.content.contains(keyword));
    }

    private BooleanExpression secretEq(Boolean secret) {
        if (secret == null) {
            return null;
        }
        return QPost.post.secret.eq(secret);
    }

    private BooleanExpression communityEq(String communityCode) {
        if (communityCode == null || communityCode.isBlank()) {
            return null;
        }
        return QPost.post.category.countryCommunity.code.eq(communityCode.toUpperCase());
    }

    private BooleanExpression onlyWithAttachments(Boolean onlyWithAttachments) {
        if (onlyWithAttachments == null) {
            return null;
        }

        if (Boolean.TRUE.equals(onlyWithAttachments)) {
            return QPost.post.attachments.any().id.isNotNull();
        }

        return QPost.post.attachments.isEmpty();
    }

    private BooleanExpression tradeTypeEq(QPostTradeInfo tradeInfo, TradeType tradeType) {
        if (tradeType == null) {
            return null;
        }
        return tradeInfo.type.eq(tradeType);
    }

    private BooleanExpression tradeStatusEq(QPostTradeInfo tradeInfo, TradeStatus tradeStatus) {
        if (tradeStatus == null) {
            return null;
        }
        return tradeInfo.status.eq(tradeStatus);
    }
}
