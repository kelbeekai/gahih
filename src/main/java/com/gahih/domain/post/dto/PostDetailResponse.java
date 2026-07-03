package com.gahih.domain.post.dto;

import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostTradeInfo;
import com.gahih.domain.post.enumtype.PostStatus;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.domain.member.enumtype.MemberRole;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String writerNickname;
    private final Long writerId;
    private final Long categoryId;
    private final String categoryName;
    private final Integer viewCount;
    private final Long zipDownloadCount;
    private final Long likeCount;
    private final Long dislikeCount;
    private final ReactionType myReactionType;

    private final TradeType tradeType;
    private final TradeStatus tradeStatus;

    private final boolean commentAllowed;
    private final boolean reactionAllowed;
    private final boolean pinned;
    private final LocalDateTime pinnedAt;

    private final boolean secret;
    private final boolean viewable;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime editedAt;

    private final List<PostAttachmentResponse> attachments;

    private final PostStatus status;
    private final boolean contentAvailable;

    private final boolean adminOriginalVisible;
    @Getter
    private final boolean userDeletedContext;

    private final boolean canReportPost;
    private final boolean canReportWriter;

    private final boolean writerAdmin;
    private final boolean mentionableWriter;

    private final boolean attachmentSectionVisible;

    private PostDetailResponse(
            Long id,
            String title,
            String content,
            String writerNickname,
            Long writerId,
            Long categoryId,
            String categoryName,
            Integer viewCount,
            Long zipDownloadCount,
            Long likeCount,
            Long dislikeCount,
            ReactionType myReactionType,
            TradeType tradeType,
            TradeStatus tradeStatus,
            boolean commentAllowed,
            boolean reactionAllowed,
            boolean pinned,
            LocalDateTime pinnedAt,
            boolean secret,
            boolean viewable,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime editedAt,
            List<PostAttachmentResponse> attachments,
            PostStatus status,
            boolean contentAvailable,
            boolean adminOriginalVisible,
            boolean userDeletedContext,
            boolean canReportPost,
            boolean canReportWriter,
            boolean writerAdmin,
            boolean mentionableWriter,
            boolean attachmentSectionVisible
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writerNickname = writerNickname;
        this.writerId = writerId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.viewCount = viewCount;
        this.zipDownloadCount = zipDownloadCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.myReactionType = myReactionType;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
        this.commentAllowed = commentAllowed;
        this.reactionAllowed = reactionAllowed;
        this.pinned = pinned;
        this.pinnedAt = pinnedAt;
        this.secret = secret;
        this.viewable = viewable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editedAt = editedAt;
        this.attachments = attachments;
        this.status = status;
        this.contentAvailable = contentAvailable;
        this.adminOriginalVisible = adminOriginalVisible;
        this.userDeletedContext = userDeletedContext;
        this.canReportPost = canReportPost;
        this.canReportWriter = canReportWriter;
        this.writerAdmin = writerAdmin;
        this.mentionableWriter = mentionableWriter;
        this.attachmentSectionVisible = attachmentSectionVisible;
    }

    public static PostDetailResponse of(
            Post post,
            List<PostAttachmentResponse> attachments,
            ReactionType myReactionType,
            PostTradeInfo tradeInfo,
            boolean secretViewable,
            boolean contentAvailable,
            boolean adminOriginalVisible,
            boolean canReportPost,
            boolean canReportWriter,
            boolean writerAdmin,
            boolean mentionableWriter,
            boolean attachmentSectionVisible
    ) {
        boolean active = post.isActive();
        boolean userDeletedContext = post.isUserDeletedContext();
        boolean effectiveAdminOriginalVisible = adminOriginalVisible && !userDeletedContext;
        boolean effectiveAttachmentSectionVisible = attachmentSectionVisible && !userDeletedContext;

        String displayContent;

        if (!secretViewable) {
            displayContent = "";
        } else if (effectiveAdminOriginalVisible) {
            displayContent = post.getContent();
        } else if (userDeletedContext && post.getStatus() == PostStatus.USER_DELETED) {
            displayContent = "작성자가 삭제한 게시글입니다.";
        } else if (userDeletedContext) {
            displayContent = post.getStatusMessage();
        } else if (contentAvailable) {
            displayContent = post.getContent();
        } else {
            displayContent = post.getStatusMessage();
        }

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                displayContent,
                resolveWriterNickname(post.getMember().getNickname(), post.getMember().getStatus()),
                post.getMember().getId(),
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getViewCount(),
                post.getZipDownloadCount(),
                post.getLikeCount(),
                post.getDislikeCount(),
                secretViewable && contentAvailable ? myReactionType : null,
                tradeInfo != null ? tradeInfo.getType() : null,
                tradeInfo != null ? tradeInfo.getStatus() : null,
                secretViewable && contentAvailable && active && post.getCategory().isCommentAllowed(),
                secretViewable && contentAvailable && active && post.getCategory().isReactionAllowed(),
                post.isPinned(),
                post.getPinnedAt(),
                post.isSecret(),
                secretViewable,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getEditedAt(),
                secretViewable && !userDeletedContext ? attachments : List.of(),
                post.getStatus(),
                contentAvailable,
                effectiveAdminOriginalVisible,
                userDeletedContext,
                canReportPost,
                canReportWriter,
                writerAdmin,
                mentionableWriter,
                effectiveAttachmentSectionVisible
        );
    }

    public boolean isLiked() {
        return myReactionType == ReactionType.LIKE;
    }

    public boolean isDisliked() {
        return myReactionType == ReactionType.DISLIKE;
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

    public String getTradeStatusToggleLabel() {
        if (!isTradePost()) {
            return "";
        }

        if (tradeStatus == TradeStatus.OPEN) {
            return tradeType.getStatusLabel(TradeStatus.CLOSED) + "로 변경";
        }

        return "다시 " + tradeType.getStatusLabel(TradeStatus.OPEN) + "로 변경";
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public LocalDateTime getDisplayTime() {
        return isEdited() ? editedAt : createdAt;
    }

    public boolean isBlockedSecretPost() {
        return secret && !viewable;
    }

    public boolean isDeletedOrBlinded() {
        return status != PostStatus.ACTIVE;
    }

    public boolean isCurrentUserDeleted() {
        return status == PostStatus.USER_DELETED;
    }

    public boolean isContentBlockedByStatus() {
        return viewable && !contentAvailable && !adminOriginalVisible;
    }

    public String getStatusLabel() {
        return switch (status) {
            case USER_DELETED -> "삭제된 게시글";
            case ADMIN_BLINDED -> "블라인드 처리됨";
            case ADMIN_DELETED -> "관리자 삭제";
            case ACTIVE -> "";
        };
    }

    public boolean isAttachmentDownloadAllowed() {
        return viewable && status == PostStatus.ACTIVE && hasActiveAttachments();
    }

    public boolean isUserActionAllowed() {
        return status == PostStatus.ACTIVE;
    }

    public boolean hasActiveAttachments() {
        return attachments.stream().anyMatch(attachment -> !attachment.isDeleted());
    }

    public boolean hasAdminDeletedAttachments() {
        return attachments.stream().anyMatch(PostAttachmentResponse::isAdminDeleted);
    }

    public String getDisplayTitle() {
        if (secret && !viewable) {
            return "비밀글입니다.";
        }

        if (userDeletedContext) {
            return switch (status) {
                case USER_DELETED -> "작성자가 삭제한 게시글입니다.";
                case ADMIN_BLINDED -> "운영 정책에 의해 블라인드 처리된 게시글입니다.";
                case ADMIN_DELETED -> "운영 정책에 의해 삭제 처리된 게시글입니다.";
                case ACTIVE -> title;
            };
        }

        if (adminOriginalVisible) {
            return title;
        }

        if (status == PostStatus.ADMIN_BLINDED) {
            return "운영 정책에 의해 블라인드 처리된 게시글입니다.";
        }

        if (status == PostStatus.ADMIN_DELETED) {
            return "운영 정책에 의해 삭제 처리된 게시글입니다.";
        }

        return title;
    }

    private static String resolveWriterNickname(String nickname, MemberStatus status) {
        if (status == MemberStatus.WITHDRAWN || status == MemberStatus.DELETED) {
            return "탈퇴한 회원";
        }
        return nickname;
    }
}
