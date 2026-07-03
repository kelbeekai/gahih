package com.gahih.domain.comment.repository;

import com.gahih.domain.comment.entity.CommentReaction;
import com.gahih.domain.reaction.enumtype.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {

    Optional<CommentReaction> findByCommentIdAndMemberId(Long commentId, Long memberId);

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteAllByCommentId(Long commentId);

    void deleteAllByCommentPostId(Long postId);

    @Query("""
            select count(cr)
            from CommentReaction cr
            where cr.comment.member.id = :memberId
              and cr.reactionType = :reactionType
              and cr.updatedAt >= :start
              and cr.updatedAt < :end
            """)
    long countTodayReceivedLikeCountByCommentMemberId(
            @Param("memberId") Long memberId,
            @Param("reactionType") ReactionType reactionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        select count(cr)
        from CommentReaction cr
        where cr.comment.post.category.countryCommunity.code = :communityCode
          and cr.comment.member.id = :memberId
          and cr.reactionType = :reactionType
          and cr.updatedAt >= :start
          and cr.updatedAt < :end
        """)
    long countTodayReceivedLikeCountByCommunityCodeAndCommentMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId,
            @Param("reactionType") ReactionType reactionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}