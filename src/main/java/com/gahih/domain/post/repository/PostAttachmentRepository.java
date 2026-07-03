package com.gahih.domain.post.repository;

import com.gahih.domain.post.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    List<PostAttachment> findAllByPostIdOrderByIdAsc(Long postId);

    Optional<PostAttachment> findByIdAndPostId(Long id, Long postId);

    @Query("select coalesce(sum(a.downloadCount), 0) from PostAttachment a where a.post.member.id = :memberId")
    long sumDownloadCountByPostMemberId(@Param("memberId") Long memberId);

    @Query("""
        select coalesce(sum(a.downloadCount), 0)
        from PostAttachment a
        where a.post.category.countryCommunity.code = :communityCode
          and a.post.member.id = :memberId
        """)
    long sumDownloadCountByCommunityCodeAndPostMemberId(
            @Param("communityCode") String communityCode,
            @Param("memberId") Long memberId
    );
}
