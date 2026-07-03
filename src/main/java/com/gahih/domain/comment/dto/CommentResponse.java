package com.gahih.domain.comment.dto;

import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.domain.member.enumtype.MemberRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long id;
    private final Long writerId;
    private final String writerNickname;
    private final String content;
    private final Long likeCount;
    private final Long dislikeCount;
    private final ReactionType myReactionType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime editedAt;
    private final CommentStatus status;

    private final boolean canReportComment;
    private final boolean canReportWriter;

    private final boolean writerAdmin;
    private final boolean mentionableWriter;

    private final String renderedContent;

    private CommentResponse(
            Long id,
            Long writerId,
            String writerNickname,
            String content,
            Long likeCount,
            Long dislikeCount,
            ReactionType myReactionType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime editedAt,
            CommentStatus status,
            boolean canReportComment,
            boolean canReportWriter,
            boolean writerAdmin,
            boolean mentionableWriter,
            String renderedContent

    ) {
        this.id = id;
        this.writerId = writerId;
        this.writerNickname = writerNickname;
        this.content = content;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.myReactionType = myReactionType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editedAt = editedAt;
        this.status = status;
        this.canReportComment = canReportComment;
        this.canReportWriter = canReportWriter;
        this.writerAdmin = writerAdmin;
        this.mentionableWriter = mentionableWriter;
        this.renderedContent = renderedContent;
    }

    public static CommentResponse from(
            Comment comment,
            ReactionType myReactionType,
            boolean canReportComment,
            boolean canReportWriter,
            boolean writerAdmin,
            boolean mentionableWriter,
            String renderedContent
    ) {
        boolean active = comment.isActive();

        return new CommentResponse(
                comment.getId(),
                comment.getMember().getId(),
                resolveWriterNickname(comment.getMember().getNickname(), comment.getMember().getStatus()),
                active ? comment.getContent() : comment.getStatusMessage(),
                active ? comment.getLikeCount() : 0L,
                active ? comment.getDislikeCount() : 0L,
                active ? myReactionType : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getEditedAt(),
                comment.getStatus(),
                canReportComment,
                canReportWriter,
                writerAdmin,
                mentionableWriter,
                active ? renderedContent : comment.getStatusMessage()
        );
    }

    public static CommentResponse from(Comment comment) {
        boolean active = comment.isActive();
        String renderedContent = active ? escapeHtml(comment.getContent()) : comment.getStatusMessage();

        return from(
                comment,
                null,
                false,
                false,
                comment.getMember().getRole() == MemberRole.ADMIN,
                false,
                renderedContent
        );
    }

    public boolean isLiked() {
        return myReactionType == ReactionType.LIKE;
    }

    public boolean isDisliked() {
        return myReactionType == ReactionType.DISLIKE;
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

    private static String escapeHtml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}