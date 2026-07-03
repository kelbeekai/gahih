package com.gahih.domain.admin.dto;

import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminPostResponse {

    private final Long id;
    private final String title;
    private final String writerNickname;
    private final String categoryName;
    private final String communityCode;
    private final String communityName;
    private final Integer viewCount;
    private final Long likeCount;
    private final Long dislikeCount;
    private final Long commentCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime editedAt;
    private final Long attachmentCount;
    private final Long attachmentDownloadCount;
    private final Long zipDownloadCount;
    private final boolean pinned;
    private final boolean pinAllowed;
    private final LocalDateTime pinnedAt;
    private final boolean secret;

    private final PostStatus status;

    private final boolean userDeletedBeforeAdminAction;

    private final Long totalReportCount;
    private final Long pendingReportCount;

    private final TradeType tradeType;
    private final TradeStatus tradeStatus;

    public AdminPostResponse(
            Long id,
            String title,
            String writerNickname,
            MemberStatus writerStatus,
            String categoryName,
            String communityCode,
            String communityName,
            Integer viewCount,
            Long likeCount,
            Long dislikeCount,
            Long commentCount,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            Long attachmentCount,
            Long attachmentDownloadCount,
            Long zipDownloadCount,
            boolean pinned,
            boolean pinAllowed,
            LocalDateTime pinnedAt,
            boolean secret,
            PostStatus status,
            boolean userDeletedBeforeAdminAction,
            Long totalReportCount,
            Long pendingReportCount,
            TradeType tradeType,
            TradeStatus tradeStatus
    ) {
        this.id = id;
        this.title = title;
        this.writerNickname = resolveWriterNickname(writerNickname, writerStatus);
        this.categoryName = categoryName;
        this.communityCode = communityCode;
        this.communityName = communityName;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
        this.attachmentCount = attachmentCount;
        this.attachmentDownloadCount = attachmentDownloadCount;
        this.zipDownloadCount = zipDownloadCount;
        this.pinned = pinned;
        this.pinAllowed = pinAllowed;
        this.pinnedAt = pinnedAt;
        this.secret = secret;
        this.status = status;
        this.userDeletedBeforeAdminAction = userDeletedBeforeAdminAction;
        this.totalReportCount = totalReportCount;
        this.pendingReportCount = pendingReportCount;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
    }

    public static AdminPostResponse from(Post post) {
        long attachmentCount = post.getAttachments().size();

        long attachmentDownloadCount = post.getAttachments()
                .stream()
                .mapToLong(attachment -> attachment.getDownloadCount() == null ? 0L : attachment.getDownloadCount())
                .sum();

        long zipDownloadCount = post.getZipDownloadCount() == null ? 0L : post.getZipDownloadCount();

        return AdminPostResponse.of(
                post.getId(),
                post.getTitle(),
                post.getMember().getNickname(),
                post.getMember().getStatus(),
                post.getCategory().getName(),
                post.getCategory().getCountryCommunity().getCode(),
                post.getCategory().getCountryCommunity().getName(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getDislikeCount(),
                0L,
                post.getCreatedAt(),
                post.getEditedAt(),
                attachmentCount,
                attachmentDownloadCount,
                zipDownloadCount,
                post.isPinned(),
                !post.isPinned(),
                post.getPinnedAt(),
                post.isSecret(),
                post.getStatus(),
                post.isUserDeletedBeforeAdminAction(),
                0L,
                0L,
                null,
                null
        );
    }

    public static AdminPostResponse of(
            Long id,
            String title,
            String writerNickname,
            MemberStatus writerStatus,
            String categoryName,
            String communityCode,
            String communityName,
            Integer viewCount,
            Long likeCount,
            Long dislikeCount,
            Long commentCount,
            LocalDateTime createdAt,
            LocalDateTime editedAt,

            Long attachmentCount,
            Long attachmentDownloadCount,
            Long zipDownloadCount,

            boolean pinned,
            boolean pinAllowed,
            LocalDateTime pinnedAt,
            boolean secret,

            PostStatus status,

            boolean userDeletedBeforeAdminAction,

            Long totalReportCount,
            Long pendingReportCount,

            TradeType tradeType,
            TradeStatus tradeStatus
    ) {
        return new AdminPostResponse(
                id,
                title,
                writerNickname,
                writerStatus,
                categoryName,
                communityCode,
                communityName,
                viewCount,
                likeCount,
                dislikeCount,
                commentCount,
                createdAt,
                editedAt,
                attachmentCount,
                attachmentDownloadCount,
                zipDownloadCount,
                pinned,
                pinAllowed,
                pinnedAt,
                secret,
                status,
                userDeletedBeforeAdminAction,
                totalReportCount,
                pendingReportCount,
                tradeType,
                tradeStatus
        );
    }

    public boolean isHasAttachments() {
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

    public String getDisplayTitle() {
        if (isUserDeletedContext()) {
            return "작성자가 삭제한 게시글입니다.";
        }

        return title;
    }

    public boolean isCanPin() {
        return pinAllowed && status == PostStatus.ACTIVE;
    }

    public boolean isCanBlind() {
        return status == PostStatus.ACTIVE
                || status == PostStatus.USER_DELETED
                || status == PostStatus.ADMIN_DELETED;
    }

    public boolean isCanRestore() {
        return status == PostStatus.ADMIN_BLINDED;
    }

    public boolean isCanAdminDelete() {
        return status == PostStatus.ACTIVE
                || status == PostStatus.USER_DELETED
                || status == PostStatus.ADMIN_BLINDED;
    }

    public boolean isCanHardDelete() {
        return status == PostStatus.ADMIN_DELETED;
    }

    private static String resolveWriterNickname(String nickname, MemberStatus status) {
        if (status == MemberStatus.WITHDRAWN || status == MemberStatus.DELETED) {
            return "탈퇴한 회원";
        }
        return nickname;
    }

    public Long getSafeTotalReportCount() {
        return totalReportCount == null ? 0L : totalReportCount;
    }

    public Long getSafePendingReportCount() {
        return pendingReportCount == null ? 0L : pendingReportCount;
    }

    public String getReportSummary() {
        return getSafePendingReportCount() + " / " + getSafeTotalReportCount();
    }

    public boolean isHasComments() {
        return commentCount != null && commentCount > 0;
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
