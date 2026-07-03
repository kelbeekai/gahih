package com.gahih.domain.comment.entity;

import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    private Long dislikeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommentStatus status;

    @Column(nullable = false)
    private boolean userDeletedBeforeAdminAction = false;

    private LocalDateTime createdAt;

    // 엔티티가 DB UPDATE 되는 모든 경우의 마지막 변경 시각
    private LocalDateTime updatedAt;

    // 사용자가 댓글 내용을 수정한 마지막 시각
    private LocalDateTime editedAt;

    private Comment(Post post, Member member, String content) {
        validatePost(post);
        validateMember(member);
        validateContent(content);

        this.post = post;
        this.member = member;
        this.content = content;
        this.likeCount = 0L;
        this.dislikeCount = 0L;
        this.status = CommentStatus.ACTIVE;
    }

    public static Comment create(Post post, Member member, String content) {
        return new Comment(post, member, content);
    }

    public void updateContent(String content) {

        if (!isEditableByUser()) {
            throw new DomainValidationException("삭제 또는 블라인드 처리된 댓글은 수정할 수 없습니다.");
        }

        validateContent(content);
        this.content = content;
        this.editedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
        if (this.dislikeCount == null) {
            this.dislikeCount = 0L;
        }

        if (this.status == null) {
            this.status = CommentStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이전 반응(before) -> 이후 반응(after) 변화에 맞춰 카운트를 동기화한다.
     * before / after 는 null 가능
     */
    public void applyReactionChange(ReactionType before, ReactionType after) {

        if (!isReactionAllowed()) {
            throw new DomainValidationException("삭제 또는 블라인드 처리된 댓글에는 반응할 수 없습니다.");
        }

        decreaseReactionCount(before);
        increaseReactionCount(after);
        validateReactionCounts();
    }

    private void increaseReactionCount(ReactionType reactionType) {
        if (reactionType == null) {
            return;
        }

        if (reactionType == ReactionType.LIKE) {
            if (this.likeCount == null) {
                this.likeCount = 0L;
            }
            this.likeCount++;
            return;
        }

        if (this.dislikeCount == null) {
            this.dislikeCount = 0L;
        }
        this.dislikeCount++;
    }

    private void decreaseReactionCount(ReactionType reactionType) {
        if (reactionType == null) {
            return;
        }

        if (reactionType == ReactionType.LIKE) {
            if (this.likeCount == null || this.likeCount <= 0) {
                throw new DomainValidationException("댓글 좋아요 수는 음수가 될 수 없습니다.");
            }
            this.likeCount--;
            return;
        }

        if (this.dislikeCount == null || this.dislikeCount <= 0) {
            throw new DomainValidationException("댓글 싫어요 수는 음수가 될 수 없습니다.");
        }
        this.dislikeCount--;
    }

    private void validateReactionCounts() {
        if (this.likeCount == null || this.likeCount < 0) {
            throw new DomainValidationException("댓글 좋아요 수는 null 또는 음수가 될 수 없습니다.");
        }
        if (this.dislikeCount == null || this.dislikeCount < 0) {
            throw new DomainValidationException("댓글 싫어요 수는 null 또는 음수가 될 수 없습니다.");
        }
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new DomainValidationException("댓글이 속한 게시글은 필수입니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new DomainValidationException("댓글 작성자는 필수입니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new DomainValidationException("댓글 내용은 비어 있을 수 없습니다.");
        }
    }

    public void deleteByUser() {
        validateActiveForStateChange();
        this.status = CommentStatus.USER_DELETED;
        this.userDeletedBeforeAdminAction = true;
    }

    public void blindByAdmin() {
        if (this.status == CommentStatus.ADMIN_BLINDED) {
            throw new DomainValidationException("이미 블라인드 처리된 댓글입니다.");
        }

        if (this.status == CommentStatus.USER_DELETED || this.userDeletedBeforeAdminAction) {
            this.userDeletedBeforeAdminAction = true;
        }

        this.status = CommentStatus.ADMIN_BLINDED;
    }

    public void deleteByAdmin() {
        if (this.status == CommentStatus.ADMIN_DELETED) {
            throw new DomainValidationException("이미 관리자 삭제 처리된 댓글입니다.");
        }

        if (this.status == CommentStatus.USER_DELETED || this.userDeletedBeforeAdminAction) {
            this.userDeletedBeforeAdminAction = true;
        }

        this.status = CommentStatus.ADMIN_DELETED;
    }

    public void restoreByAdmin() {
        if (this.status != CommentStatus.ADMIN_BLINDED) {
            throw new DomainValidationException("블라인드 처리된 댓글만 복구할 수 있습니다.");
        }

        this.status = this.userDeletedBeforeAdminAction
                ? CommentStatus.USER_DELETED
                : CommentStatus.ACTIVE;
    }

    public boolean isRestorableByAdmin() {
        return this.status == CommentStatus.ADMIN_BLINDED;
    }

    public boolean isHardDeletableByAdmin() {
        return this.status == CommentStatus.ADMIN_DELETED;
    }

    public boolean isActive() {
        return this.status == CommentStatus.ACTIVE;
    }

    public boolean isUserDeletedContext() {
        return this.status == CommentStatus.USER_DELETED || this.userDeletedBeforeAdminAction;
    }

    public boolean isEditableByUser() {
        return this.status == CommentStatus.ACTIVE;
    }

    public boolean isReactionAllowed() {
        return this.status == CommentStatus.ACTIVE;
    }

    public String getStatusMessage() {
        return switch (this.status) {
            case USER_DELETED -> "작성자가 삭제한 댓글입니다.";
            case ADMIN_BLINDED -> "운영 정책에 의해 블라인드 처리된 댓글입니다.";
            case ADMIN_DELETED -> "운영 정책에 의해 삭제 처리된 댓글입니다.";
            case ACTIVE -> "";
        };
    }

    private void validateActiveForStateChange() {
        if (this.status != CommentStatus.ACTIVE) {
            throw new DomainValidationException("이미 삭제 또는 블라인드 처리된 댓글입니다.");
        }
    }
}