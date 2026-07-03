package com.gahih.domain.post.repository;

import com.gahih.domain.member.dto.MyRecentInteractionCandidateResponse;
import com.gahih.domain.post.entity.PostReaction;
import com.gahih.domain.reaction.enumtype.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    Optional<PostReaction> findByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    void deleteAllByPostId(Long postId);

    @Query("""
            select count(pr)
            from PostReaction pr
            where pr.post.member.id = :memberId
              and pr.reactionType = :reactionType
              and pr.updatedAt >= :start
              and pr.updatedAt < :end
            """)
    long countTodayReceivedLikeCountByPostMemberId(
            @Param("memberId") Long memberId,
            @Param("reactionType") ReactionType reactionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        select count(pr)
        from PostReaction pr
        where pr.post.category.countryCommunity.code = :communityCode
          and pr.post.member.id = :memberId
          and pr.reactionType = :reactionType
          and pr.updatedAt >= :start
          and pr.updatedAt < :end
        """)
    long countTodayReceivedLikeCountByCommunityCodeAndPostMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId,
            @Param("reactionType") ReactionType reactionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select new com.gahih.domain.member.dto.MyRecentInteractionCandidateResponse(
                pr.post.id,
                pr.post.title,
                pr.post.category.name,
                pr.post.category.countryCommunity.code,
                max(pr.updatedAt)
            )
            from PostReaction pr
            where pr.post.member.id = :memberId
              and pr.member.id <> :memberId
              and pr.reactionType = :reactionType
              and pr.updatedAt >= :since
            group by pr.post.id, pr.post.title, pr.post.category.name, pr.post.category.countryCommunity.code
            order by max(pr.updatedAt) desc, pr.post.id desc
            """)
    List<MyRecentInteractionCandidateResponse> findRecentLikeInteractionPosts(
            @Param("memberId") Long memberId,
            @Param("reactionType") ReactionType reactionType,
            @Param("since") LocalDateTime since
    );

    @Query("""
        select new com.gahih.domain.member.dto.MyRecentInteractionCandidateResponse(
            pr.post.id,
            pr.post.title,
            pr.post.category.name,
            pr.post.category.countryCommunity.code,
            max(pr.updatedAt)
        )
        from PostReaction pr
        where pr.post.category.countryCommunity.code = :communityCode
          and pr.post.member.id = :memberId
          and pr.member.id <> :memberId
          and pr.reactionType = :reactionType
          and pr.updatedAt >= :since
        group by pr.post.id, pr.post.title, pr.post.category.name, pr.post.category.countryCommunity.code
        order by max(pr.updatedAt) desc, pr.post.id desc
        """)
    List<MyRecentInteractionCandidateResponse> findRecentLikeInteractionPosts(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId,
            @Param("reactionType") ReactionType reactionType,
            @Param("since") LocalDateTime since
    );
}