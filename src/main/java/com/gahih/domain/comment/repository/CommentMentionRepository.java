package com.gahih.domain.comment.repository;

import com.gahih.domain.comment.entity.CommentMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    List<CommentMention> findAllByCommentId(Long commentId);

    List<CommentMention> findAllByCommentIdOrderByMentionStartIndexAsc(Long commentId);

    void deleteAllByCommentId(Long commentId);

    @Query("""
            select cm
            from CommentMention cm
            join fetch cm.comment c
            join fetch c.post p
            join fetch p.category
            join fetch cm.mentionedMember m
            where m.id = :memberId
              and cm.createdAt >= :since
              and c.status = com.gahih.domain.comment.enumtype.CommentStatus.ACTIVE
            order by cm.createdAt desc, cm.id desc
            """)
    List<CommentMention> findRecentMentionsForMember(
            @Param("memberId") Long memberId,
            @Param("since") LocalDateTime since
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CommentMention cm
            set cm.mentionedNicknameSnapshot = :anonymizedNickname
            where cm.mentionedMember.id = :memberId
            """)
    int anonymizeMentionSnapshotsByMentionedMemberId(
            @Param("memberId") Long memberId,
            @Param("anonymizedNickname") String anonymizedNickname
    );
}