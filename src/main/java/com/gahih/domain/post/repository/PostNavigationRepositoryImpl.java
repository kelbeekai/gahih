package com.gahih.domain.post.repository;

import com.gahih.domain.comment.entity.QComment;
import com.gahih.domain.post.dto.PostListResponse;
import com.gahih.domain.post.entity.QPost;
import com.gahih.domain.post.entity.QPostAttachment;
import com.gahih.domain.post.entity.QPostTradeInfo;
import com.gahih.domain.post.enumtype.PostSortType;
import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PostNavigationRepositoryImpl implements PostNavigationRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findOrderedPostIdsForNavigation(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            PostSortType sortType
    ) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;
        QPostAttachment attachment = QPostAttachment.postAttachment;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(communityEq(communityCode));
        builder.and(categoryEq(categoryId));
        builder.and(keywordContains(keyword));
        builder.and(secretEq(secret));
        builder.and(attachmentPresenceEq(attachment, onlyWithAttachments));
        builder.and(post.status.in(PostStatus.ACTIVE, PostStatus.USER_DELETED, PostStatus.ADMIN_BLINDED));
        builder.and(tradeTypeEq(tradeInfo, tradeType));
        builder.and(tradeStatusEq(tradeInfo, tradeStatus));

        return queryFactory
                .select(post.id)
                .from(post)
                .leftJoin(comment).on(comment.post.id.eq(post.id))
                .leftJoin(attachment).on(attachment.post.id.eq(post.id))
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .where(builder)
                .groupBy(
                        post.id,
                        post.viewCount,
                        post.likeCount
                )
                .orderBy(createPostOrderSpecifiers(sortType, post, comment.id.countDistinct()))
                .fetch();
    }

    @Override
    public List<PostListResponse> findPostSummariesByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }

        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostAttachment attachment = QPostAttachment.postAttachment;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;

        return queryFactory
                .select(Projections.constructor(
                        PostListResponse.class,
                        post.id,
                        post.title,
                        post.member.id,
                        post.member.nickname,
                        post.member.status,
                        post.category.name,
                        post.viewCount,
                        post.likeCount,
                        post.createdAt,
                        post.editedAt,
                        comment.id.countDistinct(),
                        attachment.id.countDistinct(),
                        post.pinned,
                        post.pinnedAt,
                        post.secret,
                        post.status,
                        tradeInfo.type,
                        tradeInfo.status,
                        post.userDeletedBeforeAdminAction
                ))
                .from(post)
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .leftJoin(comment).on(comment.post.id.eq(post.id))
                .leftJoin(attachment).on(attachment.post.id.eq(post.id))
                .where(
                        post.id.in(postIds),
                        post.status.in(
                                PostStatus.ACTIVE,
                                PostStatus.USER_DELETED,
                                PostStatus.ADMIN_BLINDED,
                                PostStatus.ADMIN_DELETED
                        )
                )
                .groupBy(
                        post.id,
                        post.title,
                        post.member.id,
                        post.member.nickname,
                        post.member.status,
                        post.category.name,
                        post.viewCount,
                        post.likeCount,
                        post.createdAt,
                        post.editedAt,
                        post.pinned,
                        post.pinnedAt,
                        post.secret,
                        post.status,
                        tradeInfo.type,
                        tradeInfo.status,
                        post.userDeletedBeforeAdminAction
                )
                .fetch();
    }

    private BooleanExpression categoryEq(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return QPost.post.category.id.eq(categoryId);
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

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QPost.post.title.containsIgnoreCase(keyword)
                .or(QPost.post.member.nickname.containsIgnoreCase(keyword))
                .or(QPost.post.content.contains(keyword));
    }

    private OrderSpecifier<?>[] createPostOrderSpecifiers(
            PostSortType sortType,
            QPost post,
            NumberExpression<Long> commentCount
    ) {
        PostSortType safeSortType = sortType == null ? PostSortType.LATEST : sortType;

        return switch (safeSortType) {
            case OLDEST -> new OrderSpecifier[]{
                    post.id.asc()
            };
            case VIEWS -> new OrderSpecifier[]{
                    post.viewCount.desc(),
                    post.id.desc()
            };
            case LATEST -> new OrderSpecifier[]{
                    post.id.desc()
            };
            case LIKE_COUNT -> new OrderSpecifier[]{
                    post.likeCount.desc(),
                    post.id.desc()
            };
            case COMMENT_COUNT -> new OrderSpecifier[]{
                    commentCount.desc(),
                    post.id.desc()
            };
        };
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

    private BooleanExpression attachmentPresenceEq(QPostAttachment attachment, Boolean onlyWithAttachments) {
        if (onlyWithAttachments == null) {
            return null;
        }

        return onlyWithAttachments
                ? attachment.id.isNotNull()
                : attachment.id.isNull();
    }
}
