package com.gahih.domain.admin.dto;

import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.member.enumtype.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminMemberDetailCommentResponse {

    private final Long id;
    private final String communityCode;
    private final String communityName;
    private final Long postId;
    private final String postTitle;
    private final String writerNickname;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime editedAt;
    private final String status;

    private AdminMemberDetailCommentResponse(
            Long id,
            String communityCode,
            String communityName,
            Long postId,
            String postTitle,
            String writerNickname,
            String content,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            String status
    ) {
        this.id = id;
        this.communityCode = communityCode;
        this.communityName = communityName;
        this.postId = postId;
        this.postTitle = postTitle;
        this.writerNickname = writerNickname;
        this.content = content;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
        this.status = status;
    }

    public static AdminMemberDetailCommentResponse from(Comment comment) {
        return new AdminMemberDetailCommentResponse(
                comment.getId(),
                comment.getPost().getCategory().getCountryCommunity().getCode(),
                comment.getPost().getCategory().getCountryCommunity().getName(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                resolveWriterNickname(comment.getMember().getNickname(), comment.getMember().getStatus()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getEditedAt(),
                comment.getStatus().name()
        );
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public LocalDateTime getDisplayTime() {
        return isEdited() ? editedAt : createdAt;
    }

    public String getContentSummary() {
        if (content == null) {
            return "";
        }
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    private static String resolveWriterNickname(String nickname, MemberStatus status) {
        if (status == MemberStatus.WITHDRAWN || status == MemberStatus.DELETED) {
            return "탈퇴한 회원";
        }
        return nickname;
    }
}