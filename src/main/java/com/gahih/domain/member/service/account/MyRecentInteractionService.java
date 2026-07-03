package com.gahih.domain.member.service.account;

import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.dto.MyRecentInteractionCandidateResponse;
import com.gahih.domain.member.dto.MyRecentInteractionPostResponse;
import com.gahih.domain.post.repository.PostReactionRepository;
import com.gahih.domain.reaction.enumtype.ReactionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyRecentInteractionService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int LOOKBACK_DAYS = 7;
    private static final int MAX_RESULT_SIZE = 5;
    private static final int CANDIDATE_FETCH_SIZE = 10;

    private final CommentRepository commentRepository;
    private final PostReactionRepository postReactionRepository;

    public List<MyRecentInteractionPostResponse> getRecentInteractionPosts(Long memberId) {
        LocalDateTime since = LocalDateTime.now(KOREA_ZONE_ID).minusDays(LOOKBACK_DAYS);

        List<MyRecentInteractionCandidateResponse> recentCommentCandidates =
                commentRepository.findRecentCommentInteractionPosts(memberId, since, CANDIDATE_FETCH_SIZE);

        List<MyRecentInteractionCandidateResponse> recentLikeCandidates =
                postReactionRepository.findRecentLikeInteractionPosts(
                        memberId,
                        ReactionType.LIKE,
                        since
                );

        Map<Long, AggregatedInteraction> aggregatedMap = new LinkedHashMap<>();

        for (MyRecentInteractionCandidateResponse candidate : recentCommentCandidates) {
            aggregatedMap.compute(candidate.postId(), (postId, existing) -> {
                if (existing == null) {
                    return AggregatedInteraction.fromComment(candidate);
                }
                existing.mergeComment(candidate);
                return existing;
            });
        }

        for (MyRecentInteractionCandidateResponse candidate : recentLikeCandidates) {
            aggregatedMap.compute(candidate.postId(), (postId, existing) -> {
                if (existing == null) {
                    return AggregatedInteraction.fromLike(candidate);
                }
                existing.mergeLike(candidate);
                return existing;
            });
        }

        return aggregatedMap.values().stream()
                .sorted(Comparator.comparing(AggregatedInteraction::getInteractedAt).reversed())
                .limit(MAX_RESULT_SIZE)
                .map(AggregatedInteraction::toResponse)
                .collect(Collectors.toList());
    }

    public List<MyRecentInteractionPostResponse> getRecentInteractionPosts(String communityCode, Long memberId) {
        LocalDateTime since = LocalDateTime.now(KOREA_ZONE_ID).minusDays(LOOKBACK_DAYS);

        List<MyRecentInteractionCandidateResponse> recentCommentCandidates =
                commentRepository.findRecentCommentInteractionPosts(
                        communityCode.toUpperCase(),
                        memberId,
                        since,
                        CANDIDATE_FETCH_SIZE
                );

        List<MyRecentInteractionCandidateResponse> recentLikeCandidates =
                postReactionRepository.findRecentLikeInteractionPosts(
                        communityCode.toUpperCase(),
                        memberId,
                        ReactionType.LIKE,
                        since
                );

        Map<Long, AggregatedInteraction> aggregatedMap = new LinkedHashMap<>();

        for (MyRecentInteractionCandidateResponse candidate : recentCommentCandidates) {
            aggregatedMap.compute(candidate.postId(), (postId, existing) -> {
                if (existing == null) {
                    return AggregatedInteraction.fromComment(candidate);
                }
                existing.mergeComment(candidate);
                return existing;
            });
        }

        for (MyRecentInteractionCandidateResponse candidate : recentLikeCandidates) {
            aggregatedMap.compute(candidate.postId(), (postId, existing) -> {
                if (existing == null) {
                    return AggregatedInteraction.fromLike(candidate);
                }
                existing.mergeLike(candidate);
                return existing;
            });
        }

        return aggregatedMap.values().stream()
                .sorted(Comparator.comparing(AggregatedInteraction::getInteractedAt).reversed())
                .limit(MAX_RESULT_SIZE)
                .map(AggregatedInteraction::toResponse)
                .collect(Collectors.toList());
    }

    private static class AggregatedInteraction {
        private final Long postId;
        private final String title;
        private final String categoryName;
        private final String communityCode;
        private boolean hasNewComment;
        private boolean hasNewLike;
        @Getter
        private LocalDateTime interactedAt;

        private AggregatedInteraction(
                Long postId,
                String title,
                String categoryName,
                String communityCode,
                boolean hasNewComment,
                boolean hasNewLike,
                LocalDateTime interactedAt
        ) {
            this.postId = postId;
            this.title = title;
            this.categoryName = categoryName;
            this.communityCode = communityCode;
            this.hasNewComment = hasNewComment;
            this.hasNewLike = hasNewLike;
            this.interactedAt = interactedAt;
        }

        public static AggregatedInteraction fromComment(MyRecentInteractionCandidateResponse candidate) {
            return new AggregatedInteraction(
                    candidate.postId(),
                    candidate.title(),
                    candidate.categoryName(),
                    candidate.communityCode(),
                    true,
                    false,
                    candidate.interactedAt()
            );
        }

        public static AggregatedInteraction fromLike(MyRecentInteractionCandidateResponse candidate) {
            return new AggregatedInteraction(
                    candidate.postId(),
                    candidate.title(),
                    candidate.categoryName(),
                    candidate.communityCode(),
                    false,
                    true,
                    candidate.interactedAt()
            );
        }

        public void mergeComment(MyRecentInteractionCandidateResponse candidate) {
            this.hasNewComment = true;
            updateInteractedAt(candidate.interactedAt());
        }

        public void mergeLike(MyRecentInteractionCandidateResponse candidate) {
            this.hasNewLike = true;
            updateInteractedAt(candidate.interactedAt());
        }

        private void updateInteractedAt(LocalDateTime candidateTime) {
            if (candidateTime != null && (this.interactedAt == null || candidateTime.isAfter(this.interactedAt))) {
                this.interactedAt = candidateTime;
            }
        }

        public MyRecentInteractionPostResponse toResponse() {
            return MyRecentInteractionPostResponse.of(
                    postId,
                    title,
                    categoryName,
                    communityCode,
                    hasNewComment,
                    hasNewLike,
                    interactedAt
            );
        }
    }
}