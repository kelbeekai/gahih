package com.gahih.domain.post.repository;

import com.gahih.domain.comment.entity.QComment;
import com.gahih.domain.member.dto.MyPostListResponse;
import com.gahih.domain.member.enumtype.MyPostSortType;
import com.gahih.domain.post.entity.QPost;
import com.gahih.domain.post.entity.QPostAttachment;
import com.gahih.domain.post.entity.QPostTradeInfo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class PostMyPageRepositoryImpl implements PostMyPageRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MyPostListResponse> searchMyPostPage(
            String communityCode,
            Long memberId,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            MyPostSortType sortType,
            Pageable pageable
    ) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostAttachment attachment = QPostAttachment.postAttachment;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.member.id.eq(memberId));
        builder.and(communityEq(communityCode));
        builder.and(categoryEq(categoryId));
        builder.and(myPostKeywordContains(keyword));
        builder.and(secretEq(secret));
        builder.and(post.status.in(PostStatus.ACTIVE, PostStatus.USER_DELETED, PostStatus.ADMIN_BLINDED));
        builder.and(attachmentPresenceEq(attachment, onlyWithAttachments));
        builder.and(tradeTypeEq(tradeInfo, tradeType));
        builder.and(tradeStatusEq(tradeInfo, tradeStatus));

        List<MyPostListResponse> content = queryFactory
                .select(Projections.constructor(
                        MyPostListResponse.class,
                        post.id,
                        post.title,
                        post.category.name,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        post.createdAt,
                        post.editedAt,
                        comment.id.countDistinct(),
                        attachment.id.countDistinct(),
                        post.pinned,
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
                .where(builder)
                .groupBy(
                        post.id,
                        post.title,
                        post.category.name,
                        post.viewCount,
                        post.likeCount,
                        post.dislikeCount,
                        post.createdAt,
                        post.editedAt,
                        post.pinned,
                        post.secret,
                        post.status,
                        tradeInfo.type,
                        tradeInfo.status,
                        post.userDeletedBeforeAdminAction
                )
                .orderBy(createMyPostOrderSpecifiers(sortType, post, comment.id.countDistinct()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.id.countDistinct())
                .from(post)
                .leftJoin(tradeInfo).on(tradeInfo.post.id.eq(post.id))
                .leftJoin(attachment).on(attachment.post.id.eq(post.id))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public List<Long> findOrderedMyPostIdsForNavigation(
            String communityCode,
            Long memberId,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            MyPostSortType sortType
    ) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostAttachment attachment = QPostAttachment.postAttachment;
        QPostTradeInfo tradeInfo = QPostTradeInfo.postTradeInfo;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.member.id.eq(memberId));
        builder.and(communityEq(communityCode));
        builder.and(categoryEq(categoryId));
        builder.and(myPostKeywordContains(keyword));
        builder.and(secretEq(secret));
        builder.and(post.status.in(PostStatus.ACTIVE, PostStatus.USER_DELETED, PostStatus.ADMIN_BLINDED));
        builder.and(attachmentPresenceEq(attachment, onlyWithAttachments));
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
                        post.likeCount,
                        post.dislikeCount
                )
                .orderBy(createMyPostOrderSpecifiers(sortType, post, comment.id.countDistinct()))
                .fetch();
    }

    private BooleanExpression myPostKeywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QPost.post.title.containsIgnoreCase(keyword)
                .or(QPost.post.content.contains(keyword));
    }

    private OrderSpecifier<?>[] createMyPostOrderSpecifiers(
            MyPostSortType sortType,
            QPost post,
            NumberExpression<Long> commentCount
    ) {
        MyPostSortType safeSortType = sortType == null ? MyPostSortType.LATEST : sortType;

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

    private BooleanExpression categoryEq(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return QPost.post.category.id.eq(categoryId);
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
