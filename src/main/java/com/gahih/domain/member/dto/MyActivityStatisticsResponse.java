package com.gahih.domain.member.dto;

public record MyActivityStatisticsResponse(
        long totalPostCount,
        long todayPostCount,
        long totalCommentCount,
        long todayCommentCount,
        long totalReceivedLikeCount,
        long todayReceivedLikeCount,
        long totalViewCount,
        long totalAttachmentDownloadCount,
        long totalZipDownloadCount
) {
    public static MyActivityStatisticsResponse of(
            long totalPostCount,
            long todayPostCount,
            long totalCommentCount,
            long todayCommentCount,
            long totalReceivedLikeCount,
            long todayReceivedLikeCount,
            long totalViewCount,
            long totalAttachmentDownloadCount,
            long totalZipDownloadCount
    ) {
        return new MyActivityStatisticsResponse(
                totalPostCount,
                todayPostCount,
                totalCommentCount,
                todayCommentCount,
                totalReceivedLikeCount,
                todayReceivedLikeCount,
                totalViewCount,
                totalAttachmentDownloadCount,
                totalZipDownloadCount
        );
    }
}