package com.gahih.domain.member.dto;

import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyPostListResponse {


    private final Long postId;
    private final String title;
    private final String categoryName;
    private final Integer viewCount;
    private final Long likeCount;
    private final Long dislikeCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime editedAt;
    private final Long commentCount;
    private final Long attachmentCount;
    @Getter
    private final boolean pinned;
    private final boolean secret;

    private final PostStatus status;

    private final TradeType tradeType;
    private final TradeStatus tradeStatus;

    private final boolean userDeletedBeforeAdminAction;



    public MyPostListResponse(
            Long postId,
            String title,
            String categoryName,
            Integer viewCount,
            Long likeCount,
            Long dislikeCount,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            Long commentCount,
            Long attachmentCount,
            boolean pinned,
            boolean secret,
            PostStatus status,
            TradeType tradeType,
            TradeStatus tradeStatus,
            boolean userDeletedBeforeAdminAction
    ) {
        this.postId = postId;
        this.title = title;
        this.categoryName = categoryName;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
        this.commentCount = commentCount;
        this.attachmentCount = attachmentCount;
        this.pinned = pinned;
        this.secret = secret;
        this.status = status;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
        this.userDeletedBeforeAdminAction = userDeletedBeforeAdminAction;
    }

    public boolean isHasComments() {
        return commentCount > 0;
    }

    public boolean isHasAttachments() {
        if (status == PostStatus.USER_DELETED || status == PostStatus.ADMIN_BLINDED) {
            return false;
        }
        return attachmentCount > 0;
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

    public String getStatusLabel() {
        if (isUserDeletedContext()) {
            return "삭제된 게시글";
        }

        return switch (status) {
            case ADMIN_BLINDED -> "블라인드";
            case ADMIN_DELETED -> "관리자 삭제";
            case ACTIVE -> "";
            case USER_DELETED -> "삭제된 게시글";
        };
    }

    public String getDisplayTitle() {
        if (isUserDeletedContext()) {
            return "작성자가 삭제한 게시글입니다.";
        }

        if (status == PostStatus.ADMIN_BLINDED) {
            return "운영 정책에 의해 블라인드 처리된 게시글입니다.";
        }

        if (status == PostStatus.ADMIN_DELETED) {
            return "운영 정책에 의해 삭제된 게시글입니다.";
        }

        return title;
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
}
