package com.gahih.domain.admin.dto;

import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminMemberDetailPostResponse {

    private final Long id;
    private final String title;
    private final String categoryName;
    private final String writerNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime editedAt;
    private final boolean secret;
    private final String status;

    private AdminMemberDetailPostResponse(
            Long id,
            String title,
            String categoryName,
            String writerNickname,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            boolean secret,
            String status
    ) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.writerNickname = writerNickname;
        this.createdAt = createdAt;
        this.editedAt = editedAt;
        this.secret = secret;
        this.status = status;
    }

    public static AdminMemberDetailPostResponse from(Post post) {
        return new AdminMemberDetailPostResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory().getName(),
                resolveWriterNickname(post.getMember().getNickname(), post.getMember().getStatus()),
                post.getCreatedAt(),
                post.getEditedAt(),
                post.isSecret(),
                post.getStatus().name()
        );
    }

    public boolean isEdited() {
        return editedAt != null;
    }

    public LocalDateTime getDisplayTime() {
        return isEdited() ? editedAt : createdAt;
    }

    private static String resolveWriterNickname(String nickname, MemberStatus status) {
        if (status == MemberStatus.WITHDRAWN || status == MemberStatus.DELETED) {
            return "탈퇴한 회원";
        }
        return nickname;
    }
}