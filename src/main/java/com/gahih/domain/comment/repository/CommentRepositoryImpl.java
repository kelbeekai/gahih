package com.gahih.domain.comment.repository;

import com.gahih.domain.admin.dto.AdminCommentResponse;
import com.gahih.domain.admin.enumtype.AdminCommentSearchType;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.entity.QComment;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.member.dto.MyRecentInteractionCandidateResponse;
import com.gahih.domain.member.entity.QMember;
import com.gahih.domain.post.entity.QPost;
import com.gahih.domain.report.entity.QReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminCommentResponse> searchAdminCommentPage(
            String communityCode,
            String keyword,
            AdminCommentSearchType searchType,
            CommentStatus status,
            String sortName,
            Pageable pageable
    ) {
        QComment comment = QComment.comment;
        QPost post = QPost.post;
        QMember member = QMember.member;

        QReportedTarget reportedTarget = QReportedTarget.reportedTarget;

        var totalReportCountExpr = JPAExpressions
                .select(reportedTarget.totalReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.COMMENT),
                        reportedTarget.targetId.eq(comment.id)
                );

        var pendingReportCountExpr = JPAExpressions
                .select(reportedTarget.pendingReportCount)
                .from(reportedTarget)
                .where(
                        reportedTarget.targetType.eq(ReportTargetType.COMMENT),
                        reportedTarget.targetId.eq(comment.id)
                );

        BooleanBuilder builder = new BooleanBuilder();

        if (communityCode != null && !communityCode.isBlank()) {
            builder.and(post.category.countryCommunity.code.eq(communityCode.toUpperCase()));
        }

        if (status != null) {
            builder.and(comment.status.eq(status));
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();

            if (searchType == null || searchType == AdminCommentSearchType.CONTENT) {
                builder.and(comment.content.contains(trimmedKeyword));
            } else if (searchType == AdminCommentSearchType.WRITER_NICKNAME) {
                builder.and(member.nickname.containsIgnoreCase(trimmedKeyword));
            } else if (searchType == AdminCommentSearchType.POST_TITLE) {
                builder.and(post.title.containsIgnoreCase(trimmedKeyword));
            } else if (searchType == AdminCommentSearchType.POST_ID) {
                try {
                    builder.and(post.id.eq(Long.parseLong(trimmedKeyword)));
                } catch (NumberFormatException e) {
                    builder.and(comment.id.isNull());
                }
            } else if (searchType == AdminCommentSearchType.COMMENT_ID) {
                try {
                    builder.and(comment.id.eq(Long.parseLong(trimmedKeyword)));
                } catch (NumberFormatException e) {
                    builder.and(comment.id.isNull());
                }
            }
        }

        List<AdminCommentResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminCommentResponse.class,
                        comment.id,
                        post.id,
                        post.title,
                        post.category.countryCommunity.code,
                        post.category.countryCommunity.name,
                        member.id,
                        member.nickname,
                        comment.content,
                        comment.likeCount,
                        comment.dislikeCount,
                        comment.status,
                        comment.createdAt,
                        comment.updatedAt,
                        comment.editedAt,
                        totalReportCountExpr,
                        pendingReportCountExpr,
                        comment.userDeletedBeforeAdminAction
                ))
                .from(comment)
                .join(comment.post, post)
                .join(comment.member, member)
                .where(builder)
                .orderBy(getOrderSpecifier(sortName, comment))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(comment.count())
                .from(comment)
                .join(comment.post, post)
                .join(comment.member, member)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<Comment> searchCommentPage(
            Long postId,
            CommentSortType sortType,
            Pageable pageable
    ) {
        QComment comment = QComment.comment;

        CommentSortType safeSortType = sortType == null ? CommentSortType.LIKE_COUNT : sortType;

        List<Comment> content = queryFactory
                .selectFrom(comment)
                .where(
                        comment.post.id.eq(postId),
                        comment.status.ne(CommentStatus.ADMIN_DELETED)
                )
                .orderBy(getCommentOrderSpecifiers(comment, safeSortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.post.id.eq(postId),
                        comment.status.ne(CommentStatus.ADMIN_DELETED)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortName, QComment comment) {
        if ("OLDEST".equals(sortName)) {
            return comment.id.asc();
        }
        if ("LIKE_COUNT".equals(sortName)) {
            return comment.likeCount.desc();
        }
        if ("DISLIKE_COUNT".equals(sortName)) {
            return comment.dislikeCount.desc();
        }
        return comment.id.desc();
    }

    private OrderSpecifier<?>[] getCommentOrderSpecifiers(QComment comment, CommentSortType sortType) {
        return switch (sortType) {
            case LATEST -> new OrderSpecifier[]{
                    comment.id.desc()
            };
            case OLDEST -> new OrderSpecifier[]{
                    comment.id.asc()
            };
            case LIKE_COUNT -> new OrderSpecifier[]{
                    comment.likeCount.desc(),
                    comment.id.asc()
            };
        };
    }

    @Override
    public List<MyRecentInteractionCandidateResponse> findRecentCommentInteractionPosts(
            Long postOwnerId,
            LocalDateTime since,
            int limit
    ) {
        QComment comment = QComment.comment;
        QPost post = QPost.post;

        return queryFactory
                .select(Projections.constructor(
                        MyRecentInteractionCandidateResponse.class,
                        post.id,
                        post.title,
                        post.category.name,
                        post.category.countryCommunity.code,
                        comment.createdAt.max()
                ))
                .from(comment)
                .join(comment.post, post)
                .where(
                        post.member.id.eq(postOwnerId),
                        comment.member.id.ne(postOwnerId),
                        comment.createdAt.goe(since),
                        comment.status.ne(CommentStatus.ADMIN_DELETED)
                )
                .groupBy(
                        post.id,
                        post.title,
                        post.category.name,
                        post.category.countryCommunity.code
                )
                .orderBy(comment.createdAt.max().desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<MyRecentInteractionCandidateResponse> findRecentCommentInteractionPosts(
            String communityCode,
            Long postOwnerId,
            LocalDateTime since,
            int limit
    ) {
        QComment comment = QComment.comment;
        QPost post = QPost.post;

        return queryFactory
                .select(Projections.constructor(
                        MyRecentInteractionCandidateResponse.class,
                        post.id,
                        post.title,
                        post.category.name,
                        post.category.countryCommunity.code,
                        comment.createdAt.max()
                ))
                .from(comment)
                .join(comment.post, post)
                .where(
                        post.category.countryCommunity.code.eq(communityCode.toUpperCase()),
                        post.member.id.eq(postOwnerId),
                        comment.member.id.ne(postOwnerId),
                        comment.createdAt.goe(since),
                        comment.status.ne(CommentStatus.ADMIN_DELETED)
                )
                .groupBy(
                        post.id,
                        post.title,
                        post.category.name,
                        post.category.countryCommunity.code
                )
                .orderBy(comment.createdAt.max().desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }
}