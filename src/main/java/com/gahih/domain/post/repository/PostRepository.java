package com.gahih.domain.post.repository;

import com.gahih.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>,
        PostAdminRepository,
        PostMyPageRepository,
        PostListRepository,
        PostNavigationRepository {

    List<Post> findAllByOrderByIdDesc();

    /**
     * 회원이 작성한 고정글을 한 번에 찾는 메서드
     */
    List<Post> findAllByMemberIdAndPinnedTrue(Long memberId);

    List<Post> findTop5ByMemberIdOrderByIdDesc(Long memberId);

    long countByMemberId(Long memberId);

    long countByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long memberId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("select coalesce(sum(p.likeCount), 0) from Post p where p.member.id = :memberId")
    long sumLikeCountByMemberId(@Param("memberId") Long memberId);

    @Query("select coalesce(sum(p.viewCount), 0) from Post p where p.member.id = :memberId")
    long sumViewCountByMemberId(@Param("memberId") Long memberId);

    @Query("select coalesce(sum(p.zipDownloadCount), 0) from Post p where p.member.id = :memberId")
    long sumZipDownloadCountByMemberId(@Param("memberId") Long memberId);


    long countByCategory_CountryCommunity_CodeAndMemberId(String communityCode, Long memberId);

    long countByCategory_CountryCommunity_CodeAndMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String communityCode,
            Long memberId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        select coalesce(sum(p.likeCount), 0)
        from Post p
        where p.category.countryCommunity.code = :communityCode
          and p.member.id = :memberId
        """)
    long sumLikeCountByCommunityCodeAndMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId
    );

    @Query("""
        select coalesce(sum(p.viewCount), 0)
        from Post p
        where p.category.countryCommunity.code = :communityCode
          and p.member.id = :memberId
        """)
    long sumViewCountByCommunityCodeAndMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId
    );

    @Query("""
        select coalesce(sum(p.zipDownloadCount), 0)
        from Post p
        where p.category.countryCommunity.code = :communityCode
          and p.member.id = :memberId
        """)
    long sumZipDownloadCountByCommunityCodeAndMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId
    );

}