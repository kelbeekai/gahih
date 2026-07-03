package com.gahih.domain.member.dto;

import java.time.LocalDateTime;

public record MyMentionedCommentResponse(
        Long postId,
        String postTitle,
        String categoryName,
        String communityCode,
        String commentPreview,
        LocalDateTime mentionedAt
) {
    public static MyMentionedCommentResponse of(
            Long postId,
            String postTitle,
            String categoryName,
            String communityCode,
            String commentPreview,
            LocalDateTime mentionedAt
    ) {
        return new MyMentionedCommentResponse(
                postId,
                postTitle,
                categoryName,
                communityCode,
                commentPreview,
                mentionedAt
        );
    }
}