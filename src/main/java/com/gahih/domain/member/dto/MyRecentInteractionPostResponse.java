package com.gahih.domain.member.dto;

import java.time.LocalDateTime;

public record MyRecentInteractionPostResponse(
        Long postId,
        String title,
        String categoryName,
        String communityCode,
        boolean hasNewComment,
        boolean hasNewLike,
        LocalDateTime interactedAt
) {
    public static MyRecentInteractionPostResponse of(
            Long postId,
            String title,
            String categoryName,
            String communityCode,
            boolean hasNewComment,
            boolean hasNewLike,
            LocalDateTime interactedAt
    ) {
        return new MyRecentInteractionPostResponse(
                postId,
                title,
                categoryName,
                communityCode,
                hasNewComment,
                hasNewLike,
                interactedAt
        );
    }

    public String getInteractionSummary() {
        if (hasNewComment && hasNewLike) {
            return "새 댓글 · 새 좋아요";
        }
        if (hasNewComment) {
            return "새 댓글";
        }
        if (hasNewLike) {
            return "새 좋아요";
        }
        return "새 반응";
    }
}