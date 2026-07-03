package com.gahih.domain.member.service.account;

import com.gahih.domain.comment.repository.CommentReactionRepository;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.dto.MyActivityStatisticsResponse;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.domain.post.repository.PostReactionRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.reaction.enumtype.ReactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyActivityStatisticsService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostReactionRepository postReactionRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostAttachmentRepository postAttachmentRepository;

    public MyActivityStatisticsResponse getStatistics(Long memberId) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long totalPostCount = postRepository.countByMemberId(memberId);
        long todayPostCount = postRepository.countByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                memberId, startOfDay, endOfDay
        );

        long totalCommentCount = commentRepository.countByMemberId(memberId);
        long todayCommentCount = commentRepository.countByMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                memberId, startOfDay, endOfDay
        );

        long totalReceivedLikeCount =
                postRepository.sumLikeCountByMemberId(memberId)
                        + commentRepository.sumLikeCountByMemberId(memberId);

        long todayReceivedLikeCount =
                postReactionRepository.countTodayReceivedLikeCountByPostMemberId(
                        memberId, ReactionType.LIKE, startOfDay, endOfDay
                )
                        + commentReactionRepository.countTodayReceivedLikeCountByCommentMemberId(
                        memberId, ReactionType.LIKE, startOfDay, endOfDay
                );

        long totalViewCount = postRepository.sumViewCountByMemberId(memberId);
        long totalAttachmentDownloadCount = postAttachmentRepository.sumDownloadCountByPostMemberId(memberId);
        long totalZipDownloadCount = postRepository.sumZipDownloadCountByMemberId(memberId);

        return MyActivityStatisticsResponse.of(
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

    public MyActivityStatisticsResponse getStatistics(String communityCode, Long memberId) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long totalPostCount = postRepository.countByCategory_CountryCommunity_CodeAndMemberId(
                communityCode.toUpperCase(),
                memberId
        );
        long todayPostCount = postRepository.countByCategory_CountryCommunity_CodeAndMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                communityCode.toUpperCase(),
                memberId,
                startOfDay,
                endOfDay
        );

        long totalCommentCount = commentRepository.countByPost_Category_CountryCommunity_CodeAndMemberId(
                communityCode.toUpperCase(),
                memberId
        );
        long todayCommentCount = commentRepository.countByPost_Category_CountryCommunity_CodeAndMemberIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                communityCode.toUpperCase(),
                memberId,
                startOfDay,
                endOfDay
        );

        long totalReceivedLikeCount =
                postRepository.sumLikeCountByCommunityCodeAndMemberId(communityCode.toUpperCase(), memberId)
                        + commentRepository.sumLikeCountByCommunityCodeAndMemberId(communityCode.toUpperCase(), memberId);

        long todayReceivedLikeCount =
                postReactionRepository.countTodayReceivedLikeCountByCommunityCodeAndPostMemberId(
                        communityCode.toUpperCase(),
                        memberId,
                        ReactionType.LIKE,
                        startOfDay,
                        endOfDay
                )
                        + commentReactionRepository.countTodayReceivedLikeCountByCommunityCodeAndCommentMemberId(
                        communityCode.toUpperCase(),
                        memberId,
                        ReactionType.LIKE,
                        startOfDay,
                        endOfDay
                );

        long totalViewCount = postRepository.sumViewCountByCommunityCodeAndMemberId(
                communityCode.toUpperCase(),
                memberId
        );
        long totalAttachmentDownloadCount = postAttachmentRepository.sumDownloadCountByCommunityCodeAndPostMemberId(
                communityCode.toUpperCase(),
                memberId
        );
        long totalZipDownloadCount = postRepository.sumZipDownloadCountByCommunityCodeAndMemberId(
                communityCode.toUpperCase(),
                memberId
        );

        return MyActivityStatisticsResponse.of(
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