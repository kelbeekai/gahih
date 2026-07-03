package com.gahih.domain.post.entity;

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
        name = "post_reaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_reaction_member_post",
                        columnNames = {"member_id", "post_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReactionType reactionType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private PostReaction(Member member, Post post, ReactionType reactionType) {
        validateMember(member);
        validatePost(post);
        validateReactionType(reactionType);

        this.member = member;
        this.post = post;
        this.reactionType = reactionType;
    }

    public static PostReaction create(Member member, Post post, ReactionType reactionType) {
        return new PostReaction(member, post, reactionType);
    }

    public void changeReactionType(ReactionType reactionType) {
        validateReactionType(reactionType);
        this.reactionType = reactionType;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new DomainValidationException("게시글 반응 회원은 필수입니다.");
        }
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new DomainValidationException("게시글 반응 대상 게시글은 필수입니다.");
        }
    }

    private void validateReactionType(ReactionType reactionType) {
        if (reactionType == null) {
            throw new DomainValidationException("게시글 반응 타입은 필수입니다.");
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