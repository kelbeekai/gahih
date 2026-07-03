package com.gahih.domain.comment.repository;

import com.gahih.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    List<Comment> findAllByPostIdOrderByIdAsc(Long postId);

    List<Comment> findTop5ByMemberIdOrderByIdDesc(Long memberId);

    void deleteAllByPostId(Long postId);

    long countByMemberId(Long memberId);

    long countByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long memberId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("select coalesce(sum(c.likeCount), 0) from Comment c where c.member.id = :memberId")
    long sumLikeCountByMemberId(@Param("memberId") Long memberId);

    long countByPost_Category_CountryCommunity_CodeAndMemberId(String communityCode, Long memberId);

    long countByPost_Category_CountryCommunity_CodeAndMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String communityCode,
            Long memberId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        select coalesce(sum(c.likeCount), 0)
        from Comment c
        where c.post.category.countryCommunity.code = :communityCode
          and c.member.id = :memberId
        """)
    long sumLikeCountByCommunityCodeAndMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId
    );
}
