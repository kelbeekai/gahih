package com.gahih.domain.comment.entity;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "comment_reaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comment_reaction_member_comment",
                        columnNames = {"member_id", "comment_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReactionType reactionType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private CommentReaction(Member member, Comment comment, ReactionType reactionType) {
        validateMember(member);
        validateComment(comment);
        validateReactionType(reactionType);

        this.member = member;
        this.comment = comment;
        this.reactionType = reactionType;
    }

    public static CommentReaction create(Member member, Comment comment, ReactionType reactionType) {
        return new CommentReaction(member, comment, reactionType);
    }

    public void changeReactionType(ReactionType reactionType) {
        validateReactionType(reactionType);
        this.reactionType = reactionType;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new DomainValidationException("댓글 반응 회원은 필수입니다.");
        }
    }

    private void validateComment(Comment comment) {
        if (comment == null) {
            throw new DomainValidationException("댓글 반응 대상 댓글은 필수입니다.");
        }
    }

    private void validateReactionType(ReactionType reactionType) {
        if (reactionType == null) {
            throw new DomainValidationException("댓글 반응 타입은 필수입니다.");
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}