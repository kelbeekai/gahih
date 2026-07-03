package com.gahih.domain.post.dto;

import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostListResponse {

    private final Long id;
    private final String title;
    private final Long writerId;
    private final String writerNickname;

    private final String categoryName;
    private final Integer viewCount;
    private final Long likeCount;

    private final LocalDateTime createdAt;
    private final LocalDateTime editedAt;

    private final Long commentCount;
    private final Long attachmentCount;

    private final boolean pinned;
    private final LocalDateTime pinnedAt;
    private final boolean secret;

    private final TradeType tradeType;
    private final TradeStatus tradeStatus;

    private final PostStatus status;

    private final boolean userDeletedBeforeAdminAction;

    private final boolean secretTitleVisible;

    /**
     * 페이지네이션 적용 이전 메서드
     */
    public static PostListResponse from(Post post) {
        return of(
                post.getId(),
                post.getTitle(),
                post.getMember().getId(),
                post.getMember().getNickname(),
                post.getMember().getStatus(),
                post.getCategory().getName(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getEditedAt(),
                0L,
                (long) post.getAttachments().size(),
                post.isPinned(),
                post.getPinnedAt(),
                post.isSecret(),
                post.getStatus(),
                null,
                null,
                post.isUserDeletedBeforeAdminAction()
        );
    }

    public PostListResponse(
            Long id,
            String title,
            Long writerId,
            String writerNickname,
            MemberStatus writerStatus,
            String categoryName,
            Integer viewCount,
            Long likeCount,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            Long commentCount,
            Long attachmentCount,
            boolean pinned,
            LocalDateTime pinnedAt,
            boolean secret,
            PostStatus status,
            TradeType tradeType,
            TradeStatus tradeStatus,
            boolean userDeletedBeforeAdminAction
    ) {
        this.id = id;
        this.title = title;
        this.writerId = writerId;
        this.writerNickname = resolveWriterNickname(writerNickname, writerStatus);
        this.categoryName = categoryName;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
        this.commentCount = commentCount;
        this.attachmentCount = attachmentCount;
        this.pinned = pinned;
        this.pinnedAt = pinnedAt;
        this.secret = secret;
        this.status = status;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
        this.secretTitleVisible = !secret;
        this.userDeletedBeforeAdminAction = userDeletedBeforeAdminAction;
    }

    /**
     * 비밀글 제목 표시용 복사 생성자
     */
    private PostListResponse(PostListResponse source, boolean secretTitleVisible) {
        this.id = source.id;
        this.title = source.title;
        this.writerId = source.writerId;
        this.writerNickname = source.writerNickname;
        this.categoryName = source.categoryName;
        this.viewCount = source.viewCount;
        this.likeCount = source.likeCount;
        this.createdAt = source.createdAt;
        this.editedAt = source.editedAt;
        this.commentCount = source.commentCount;
        this.attachmentCount = source.attachmentCount;
        this.pinned = source.pinned;
        this.pinnedAt = source.pinnedAt;
        this.secret = source.secret;
        this.status = source.status;
        this.tradeType = source.tradeType;
        this.tradeStatus = source.tradeStatus;
        this.userDeletedBeforeAdminAction = source.userDeletedBeforeAdminAction;
        this.secretTitleVisible = secretTitleVisible;
    }

    public static PostListResponse of(
            Long id,
            String title,
            Long writerId,
            String writerNickname,
            MemberStatus writerStatus,
            String categoryName,
            Integer viewCount,
            Long likeCount,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            Long commentCount,
            Long attachmentCount,
            boolean pinned,
            LocalDateTime pinnedAt,
            boolean secret,
            PostStatus status,
            TradeType tradeType,
            TradeStatus tradeStatus,
            boolean userDeletedBeforeAdminAction
    ) {
        return new PostListResponse(
                id,
                title,
                writerId,
                writerNickname,
                writerStatus,
                categoryName,
                viewCount,
                likeCount,
                createdAt,
                editedAt,
                commentCount,
                attachmentCount,
                pinned,
                pinnedAt,
                secret,
                status,
                tradeType,
                tradeStatus,
                userDeletedBeforeAdminAction
        );
    }

    public boolean isHasComments() {
        return commentCount != null && commentCount > 0;
    }

    public boolean isHasAttachments() {
        if (status == PostStatus.USER_DELETED || status == PostStatus.ADMIN_BLINDED) {
            return false;
        }
        return attachmentCount != null && attachmentCount > 0;
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public LocalDateTime getDisplayTime() {
        return isEdited() ? editedAt : createdAt;
    }

    public boolean isDeletedOrBlinded() {
        return status != PostStatus.ACTIVE;
    }

    public boolean isUserDeletedContext() {
        return status == PostStatus.USER_DELETED || userDeletedBeforeAdminAction;
    }

    public boolean isCurrentUserDeleted() {
        return status == PostStatus.USER_DELETED;
    }

    public String getStatusLabel() {
        return switch (status) {
            case USER_DELETED -> "삭제된 게시글";
            case ADMIN_BLINDED -> "블라인드 처리됨";
            case ADMIN_DELETED -> "관리자 삭제";
            case ACTIVE -> "";
        };
    }

    public boolean isTradePost() {
        return status == PostStatus.ACTIVE
                && tradeType != null
                && tradeStatus != null;
    }

    public boolean isTradeOpen() {
        return isTradePost() && tradeStatus == TradeStatus.OPEN;
    }

    public boolean isTradeClosed() {
        return isTradePost() && tradeStatus == TradeStatus.CLOSED;
    }

    public String getTradeTypeDisplayName() {
        if (tradeType == null) {
            return "";
        }
        return tradeType.getDisplayName();
    }

    public String getTradeStatusLabel() {
        if (!isTradePost()) {
            return "";
        }
        return tradeType.getStatusLabel(tradeStatus);
    }

    public String getTradeStatusClass() {
        if (!isTradePost()) {
            return "";
        }

        if (tradeStatus == TradeStatus.CLOSED) {
            return "trade-status-closed";
        }

        return switch (tradeType) {
            case GIVE -> "trade-status-open trade-type-give";
            case SELL -> "trade-status-open trade-type-sell";
            case WANTED -> "trade-status-open trade-type-wanted";
        };
    }

    private static String resolveWriterNickname(String nickname, MemberStatus status) {
        if (status == MemberStatus.WITHDRAWN || status == MemberStatus.DELETED) {
            return "탈퇴한 회원";
        }
        return nickname;
    }

    public String getDisplayTitle() {
        if (secret && !secretTitleVisible) {
            return "비밀글입니다.";
        }

        if (isUserDeletedContext()) {
            return "작성자가 삭제한 게시글입니다.";
        }

        if (status == PostStatus.ADMIN_BLINDED) {
            return "운영 정책에 의해 블라인드 처리된 게시글입니다.";
        }

        if (status == PostStatus.ADMIN_DELETED) {
            return "운영 정책에 의해 삭제 처리된 게시글입니다.";
        }

        return title;
    }

    public PostListResponse withSecretTitleVisible(Long loginMemberId, boolean isAdmin) {
        boolean visible = !secret
                || isAdmin
                || (loginMemberId != null && writerId != null && writerId.equals(loginMemberId));

        return new PostListResponse(this, visible);
    }

}
