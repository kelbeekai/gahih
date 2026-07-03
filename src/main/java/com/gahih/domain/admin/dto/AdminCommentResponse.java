package com.gahih.domain.admin.dto;

import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.member.enumtype.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminCommentResponse {

    private final Long id;
    private final Long postId;
    private final String postTitle;
    private final String communityCode;
    private final String communityName;
    private final Long writerId;
    private final String writerNickname;
    private final String content;
    private final Long likeCount;
    private final Long dislikeCount;
    private final CommentStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime editedAt;

    private final Long totalReportCount;
    private final Long pendingReportCount;

    private final boolean userDeletedBeforeAdminAction;

    public AdminCommentResponse(
            Long id,
            Long postId,
            String postTitle,
            String communityCode,
            String communityName,
            Long writerId,
            String writerNickname,
            String content,
            Long likeCount,
            Long dislikeCount,
            CommentStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime editedAt,
            Long totalReportCount,
            Long pendingReportCount,
            boolean userDeletedBeforeAdminAction
    ) {
        this.id = id;
        this.postId = postId;
        this.postTitle = postTitle;
        this.communityCode = communityCode;
        this.communityName = communityName;
        this.writerId = writerId;
        this.writerNickname = writerNickname;
        this.content = content;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editedAt = editedAt;
        this.totalReportCount = totalReportCount;
        this.pendingReportCount = pendingReportCount;
        this.userDeletedBeforeAdminAction = userDeletedBeforeAdminAction;
    }

    public static AdminCommentResponse from(Comment comment) {
        return new AdminCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getPost().getCategory().getCountryCommunity().getCode(),
                comment.getPost().getCategory().getCountryCommunity().getName(),
                comment.getMember().getId(),
                resolveWriterNickname(comment.getMember().getNickname(), comment.getMember().getStatus()),
                comment.isUserDeletedContext() ? "" : comment.getContent(),
                comment.isActive() ? comment.getLikeCount() : 0L,
                comment.isActive() ? comment.getDislikeCount() : 0L,
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getEditedAt(),
                0L,
                0L,
                comment.isUserDeletedBeforeAdminAction()
        );
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public LocalDateTime getDisplayTime() {
        return isEdited() ? updatedAt : createdAt;
    }

    public boolean isDeletedOrBlinded() {
        return status != CommentStatus.ACTIVE;
    }

    public boolean isUserDeletedContext() {
        return status == CommentStatus.USER_DELETED || userDeletedBeforeAdminAction;
    }

    public boolean isCurrentUserDeleted() {
        return status == CommentStatus.USER_DELETED;
    }

    public String getDisplayStatusLabel() {
        return switch (status) {
            case USER_DELETED -> "삭제된 댓글";
            case ADMIN_BLINDED -> "블라인드 처리됨";
            case ADMIN_DELETED -> "관리자 삭제";
            case ACTIVE -> "";
        };
    }

    public boolean isCanAdminBlind() {
        return status == CommentStatus.ACTIVE
                || status == CommentStatus.USER_DELETED
                || status == CommentStatus.ADMIN_DELETED;
    }

    public boolean isCanAdminRestore() {
        return status == CommentStatus.ADMIN_BLINDED;
    }

    public boolean isCanAdminDelete() {
        return status == CommentStatus.ACTIVE
                || status == CommentStatus.USER_DELETED
                || status == CommentStatus.ADMIN_BLINDED;
    }

    public boolean isCanAdminHardDelete() {
        return status == CommentStatus.ADMIN_DELETED;
    }

    private static String resolveWriterNickname(String nickname, MemberStatus status) {
        if (status == MemberStatus.WITHDRAWN || status == MemberStatus.DELETED) {
            return "탈퇴한 회원";
        }
        return nickname;
    }

    public String getContentSummary() {
        if (isUserDeletedContext()) {
            return "";
        }

        if (content == null) {
            return "";
        }

        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
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
}