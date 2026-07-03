package com.gahih.domain.post.entity;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    private static final int TITLE_MAX_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private Long zipDownloadCount;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    private Long dislikeCount;

    @Column(nullable = false)
    private boolean pinned;

    private LocalDateTime pinnedAt;

    @Column(nullable = false)
    private boolean secret;

    @Column(nullable = false)
    private boolean userDeletedBeforeAdminAction = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostStatus status;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostAttachment> attachments = new ArrayList<>();

    private LocalDateTime createdAt;

    // 엔티티가 DB UPDATE 되는 모든 경우의 마지막 변경 시각
    private LocalDateTime updatedAt;

    // 사용자가 게시글 내용을 수정한 마지막 시각
    private LocalDateTime editedAt;

    private Post(Member member, Category category, String title, String content, Integer viewCount, boolean secret) {
        validateMember(member);
        validateCategory(category);
        validateTitle(title);
        validateContent(content);
        validateViewCount(viewCount);

        this.member = member;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.zipDownloadCount = 0L;
        this.likeCount = 0L;
        this.dislikeCount = 0L;
        this.pinned = false;
        this.pinnedAt = null;
        this.secret = secret;
        this.status = PostStatus.ACTIVE;
    }

    public static Post create(Member member, Category category, String title, String content) {
        return new Post(member, category, title, content, 0, false);
    }

    public static Post create(Member member, Category category, String title, String content, boolean secret) {
        return new Post(member, category, title, content, 0, secret);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.zipDownloadCount == null) {
            this.zipDownloadCount = 0L;
        }
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
        if (this.dislikeCount == null) {
            this.dislikeCount = 0L;
        }

        if (this.status == null) {
            this.status = PostStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(Category category, String title, String content) {
        update(category, title, content, this.secret);
    }

    public void update(Category category, String title, String content, boolean secret) {

        if (!isEditableByUser()) {
            throw new DomainValidationException("삭제 또는 블라인드 처리된 게시글은 수정할 수 없습니다.");
        }

        validateCategory(category);
        validateTitle(title);
        validateContent(content);

        this.category = category;
        this.title = title;
        this.content = content;
        this.secret = secret;
        this.editedAt = LocalDateTime.now();
    }

    public void pin() {
        if (this.pinned) {
            throw new DomainValidationException("이미 고정된 게시글입니다.");
        }
        this.pinned = true;
        this.pinnedAt = LocalDateTime.now();
    }

    public void unpin() {
        if (!this.pinned) {
            throw new DomainValidationException("고정글이 아닌 게시글입니다.");
        }
        this.pinned = false;
        this.pinnedAt = null;
    }

    public void increaseViewCount() {

        if (!isViewCountAllowed()) {
            return;
        }

        if (!isActive()) {
            return;
        }

        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    public void increaseZipDownloadCount() {

        if (!isViewCountAllowed()) {
            return;
        }

        if (!isActive()) {
            return;
        }

        if (this.zipDownloadCount == null) {
            this.zipDownloadCount = 0L;
        }
        this.zipDownloadCount++;
    }

    /**
     * 이전 반응(before) -> 이후 반응(after) 변화에 맞춰 카운트를 동기화한다.
     * before / after 는 null 가능
     */
    public void applyReactionChange(ReactionType before, ReactionType after) {

        if (!isReactionAllowed()) {
            throw new DomainValidationException("삭제 또는 블라인드 처리된 게시글에는 반응할 수 없습니다.");
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
                throw new DomainValidationException("게시글 좋아요 수는 음수가 될 수 없습니다.");
            }
            this.likeCount--;
            return;
        }

        if (this.dislikeCount == null || this.dislikeCount <= 0) {
            throw new DomainValidationException("게시글 싫어요 수는 음수가 될 수 없습니다.");
        }
        this.dislikeCount--;
    }

    private void validateReactionCounts() {
        if (this.likeCount == null || this.likeCount < 0) {
            throw new DomainValidationException("게시글 좋아요 수는 null 또는 음수가 될 수 없습니다.");
        }
        if (this.dislikeCount == null || this.dislikeCount < 0) {
            throw new DomainValidationException("게시글 싫어요 수는 null 또는 음수가 될 수 없습니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new DomainValidationException("게시글 작성자는 필수입니다.");
        }
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new DomainValidationException("카테고리는 필수입니다.");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("게시글 제목은 비어 있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new DomainValidationException("게시글 제목은 200자를 초과할 수 없습니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new DomainValidationException("게시글 내용은 비어 있을 수 없습니다.");
        }
    }

    private void validateViewCount(Integer viewCount) {
        if (viewCount == null) {
            throw new DomainValidationException("조회수는 null일 수 없습니다.");
        }
        if (viewCount < 0) {
            throw new DomainValidationException("조회수는 음수가 될 수 없습니다.");
        }
    }

    private void validateAttachment(PostAttachment attachment) {
        if (attachment == null) {
            throw new DomainValidationException("첨부파일은 null일 수 없습니다.");
        }
    }

    public void addAttachment(PostAttachment attachment) {
        validateAttachment(attachment);

        if (!this.attachments.contains(attachment)) {
            this.attachments.add(attachment);
        }
        attachment.changePost(this);
    }

    public void removeAttachment(PostAttachment attachment) {
        if (attachment == null) {
            return;
        }

        this.attachments.remove(attachment);
        attachment.changePost(null);
    }

    public void deleteByUser() {
        validateActiveForStateChange();
        this.status = PostStatus.USER_DELETED;
        this.userDeletedBeforeAdminAction = true;
        clearPinIfNeeded();
    }

    public void blindByAdmin() {
        if (this.status == PostStatus.ADMIN_BLINDED) {
            throw new DomainValidationException("이미 블라인드 처리된 게시글입니다.");
        }

        if (this.status == PostStatus.USER_DELETED || this.userDeletedBeforeAdminAction) {
            this.userDeletedBeforeAdminAction = true;
        }

        this.status = PostStatus.ADMIN_BLINDED;
        clearPinIfNeeded();
    }

    public void deleteByAdmin() {
        if (this.status == PostStatus.ADMIN_DELETED) {
            throw new DomainValidationException("이미 관리자 삭제 처리된 게시글입니다.");
        }

        if (this.status == PostStatus.USER_DELETED || this.userDeletedBeforeAdminAction) {
            this.userDeletedBeforeAdminAction = true;
        }

        this.status = PostStatus.ADMIN_DELETED;
        clearPinIfNeeded();
    }

    public void restoreByAdmin() {
        if (this.status != PostStatus.ADMIN_BLINDED) {
            throw new DomainValidationException("블라인드 처리된 게시글만 복구할 수 있습니다.");
        }

        this.status = this.userDeletedBeforeAdminAction
                ? PostStatus.USER_DELETED
                : PostStatus.ACTIVE;
    }

    public boolean isRestorableByAdmin() {
        return this.status == PostStatus.ADMIN_BLINDED;
    }

    public boolean isBlindableByAdmin() {
        return this.status == PostStatus.ACTIVE
                || this.status == PostStatus.USER_DELETED
                || this.status == PostStatus.ADMIN_DELETED;
    }

    public boolean isHardDeletableByAdmin() {
        return this.status == PostStatus.ADMIN_DELETED;
    }

    public boolean isActive() {
        return this.status == PostStatus.ACTIVE;
    }

    public boolean isUserDeleted() {
        return this.status == PostStatus.USER_DELETED;
    }

    public boolean isUserDeletedContext() {
        return this.status == PostStatus.USER_DELETED || this.userDeletedBeforeAdminAction;
    }

    public boolean isAdminBlinded() {
        return this.status == PostStatus.ADMIN_BLINDED;
    }

    public boolean isAdminDeleted() {
        return this.status == PostStatus.ADMIN_DELETED;
    }

    public boolean isVisibleInPublicList() {
        return this.status == PostStatus.ACTIVE || this.status == PostStatus.ADMIN_BLINDED;
    }

    public boolean isEditableByUser() {
        return this.status == PostStatus.ACTIVE;
    }

    public boolean isReactionAllowed() {
        return this.status == PostStatus.ACTIVE;
    }

    public boolean isViewCountAllowed() {
        return this.status == PostStatus.ACTIVE;
    }

    public String getStatusMessage() {
        return switch (this.status) {
            case USER_DELETED -> "사용자가 삭제한 게시글입니다.";
            case ADMIN_BLINDED -> "운영 정책에 의해 블라인드 처리된 게시글입니다.";
            case ADMIN_DELETED -> "운영 정책에 의해 삭제 처리된 게시글입니다.";
            case ACTIVE -> "";
        };
    }

    private void validateActiveForStateChange() {
        if (this.status != PostStatus.ACTIVE) {
            throw new DomainValidationException("이미 삭제 또는 블라인드 처리된 게시글입니다.");
        }
    }

    private void clearPinIfNeeded() {
        if (this.pinned) {
            this.pinned = false;
            this.pinnedAt = null;
        }
    }
}