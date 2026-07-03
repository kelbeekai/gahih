package com.gahih.domain.member.dto;

import java.time.LocalDateTime;

public record MyRecentInteractionCandidateResponse(
        Long postId,
        String title,
        String categoryName,
        String communityCode,
        LocalDateTime interactedAt
) {
    public static MyRecentInteractionCandidateResponse of(
            Long postId,
            String title,
            String categoryName,
            String communityCode,
            LocalDateTime interactedAt
    ) {
        return new MyRecentInteractionCandidateResponse(
                postId,
                title,
                categoryName,
                communityCode,
                interactedAt
        );
    }
}