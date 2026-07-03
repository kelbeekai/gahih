package com.gahih.domain.comment.entity;

import com.gahih.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "comment_mention",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comment_mention_comment_member",
                        columnNames = {"comment_id", "mentioned_member_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentioned_member_id")
    private Member mentionedMember;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "mentioned_nickname_snapshot", nullable = false, length = 30)
    private String mentionedNicknameSnapshot;

    @Column(name = "mention_start_index", nullable = false)
    private Integer mentionStartIndex;

    @Column(name = "mention_end_index_exclusive", nullable = false)
    private Integer mentionEndIndexExclusive;

    private CommentMention(
            Comment comment,
            Member mentionedMember,
            String mentionedNicknameSnapshot,
            Integer mentionStartIndex,
            Integer mentionEndIndexExclusive
    ) {
        this.comment = comment;
        this.mentionedMember = mentionedMember;
        this.mentionedNicknameSnapshot = mentionedNicknameSnapshot;
        this.mentionStartIndex = mentionStartIndex;
        this.mentionEndIndexExclusive = mentionEndIndexExclusive;
    }

    public static CommentMention create(
            Comment comment,
            Member mentionedMember,
            String mentionedNicknameSnapshot,
            int mentionStartIndex,
            int mentionEndIndexExclusive
    ) {
        return new CommentMention(
                comment,
                mentionedMember,
                mentionedNicknameSnapshot,
                mentionStartIndex,
                mentionEndIndexExclusive
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}