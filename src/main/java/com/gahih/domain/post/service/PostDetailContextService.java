package com.gahih.domain.post.service;

import com.gahih.domain.admin.dto.AdminPostResponse;
import com.gahih.domain.admin.dto.AdminPostSearchCondition;
import com.gahih.domain.member.dto.MyPostListResponse;
import com.gahih.domain.member.dto.MyPostSearchCondition;
import com.gahih.domain.post.dto.*;
import com.gahih.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDetailContextService {

    private static final int MAX_PINNED_POST_COUNT = 5;

    private final PostRepository postRepository;

    public PostNavigationResponse findPostNavigation(
            Long currentPostId,
            PostSearchCondition condition,
            Long loginMemberId,
            boolean isAdmin
    ) {
        List<Long> orderedPostIds = postRepository.findOrderedPostIdsForNavigation(
                condition.getCommunityCode(),
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachmentsOrNull(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSort()
        );

/*      이전/다음 글을 한 번 직접 계산해놓고, 마지막에 다시 반환값을 호출하는지 확인. 즉 계산 로직이 중복이면 삭제하기.
        if (orderedPostIds.isEmpty()) {
            return PostNavigationResponse.empty();
        }

        int currentIndex = orderedPostIds.indexOf(currentPostId);
        if (currentIndex < 0) {
            return PostNavigationResponse.empty();
        }

        Long previousPostId = currentIndex > 0 ? orderedPostIds.get(currentIndex - 1) : null;
        Long nextPostId = currentIndex < orderedPostIds.size() - 1 ? orderedPostIds.get(currentIndex + 1) : null;

        List<Long> targetIds = new ArrayList<>();
        if (previousPostId != null) {
            targetIds.add(previousPostId);
        }
        if (nextPostId != null) {
            targetIds.add(nextPostId);
        }

        if (targetIds.isEmpty()) {
            return PostNavigationResponse.empty();
        }

        Map<Long, PostListResponse> summaryMap = postRepository.findPostSummariesByIds(targetIds)
                .stream()
                .map(post -> post.withSecretTitleVisible(loginMemberId, isAdmin))
                .collect(Collectors.toMap(PostListResponse::getId, Function.identity()));

        PostNavigationItemResponse previousPost = null;
        if (previousPostId != null) {
            PostListResponse previousSummary = summaryMap.get(previousPostId);
            if (previousSummary != null) {
                int previousPage = ((currentIndex - 1) / condition.getSafeSize()) + 1;
                previousPost = PostNavigationItemResponse.of(
                        previousSummary.getId(),
                        previousPage,
                        previousSummary.getDisplayTitle()
                );
            }
        }

        PostNavigationItemResponse nextPost = null;
        if (nextPostId != null) {
            PostListResponse nextSummary = summaryMap.get(nextPostId);
            if (nextSummary != null) {
                int nextPage = ((currentIndex + 1) / condition.getSafeSize()) + 1;
                nextPost = PostNavigationItemResponse.of(
                        nextSummary.getId(),
                        nextPage,
                        nextSummary.getDisplayTitle()
                );
            }
        }
*/

        return buildNavigationResponse(
                currentPostId,
                orderedPostIds,
                condition.getSafeSize(),
                loginMemberId,
                isAdmin
        );
    }

    public Page<PostListResponse> searchMyPostsForDetail(
            String communityCode,
            Long memberId,
            PostDetailContext detailContext,
            Long loginMemberId,
            boolean isAdmin
    ) {
        MyPostSearchCondition condition = detailContext.toMyPostSearchCondition();

        Page<MyPostListResponse> page = postRepository.searchMyPostPage(
                communityCode,
                memberId,
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachmentsOrNull(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSort(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );

        List<Long> ids = page.getContent().stream()
                .map(MyPostListResponse::getPostId)
                .toList();

        return toSummaryPage(ids, page.getPageable(), page.getTotalElements(), loginMemberId, isAdmin);
    }

    public Page<PostListResponse> searchAdminPostsForDetail(
            String communityCode,
            PostDetailContext detailContext,
            Long loginMemberId,
            boolean isAdmin
    ) {
        AdminPostSearchCondition condition = detailContext.toAdminPostSearchCondition();

        Page<AdminPostResponse> page = postRepository.searchAdminPostPage(
                communityCode,
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachments(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSortName(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );

        List<Long> ids = page.getContent().stream()
                .map(AdminPostResponse::getId)
                .toList();

        return toSummaryPage(ids, page.getPageable(), page.getTotalElements(), loginMemberId, isAdmin);
    }

    public List<PostListResponse> findAdminPinnedPostsForDetail(
            String communityCode,
            PostDetailContext detailContext,
            Long loginMemberId,
            boolean isAdmin
    ) {
        AdminPostSearchCondition condition = detailContext.toAdminPostSearchCondition();

        List<Long> ids = postRepository.findPinnedAdminPostList(
                        communityCode,
                        condition.getCategoryId(),
                        condition.getTradeType(),
                        condition.getTradeStatus(),
                        MAX_PINNED_POST_COUNT
                )
                .stream()
                .map(AdminPostResponse::getId)
                .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, PostListResponse> summaryMap = postRepository.findPostSummariesByIds(ids)
                .stream()
                .map(post -> post.withSecretTitleVisible(loginMemberId, isAdmin))
                .collect(Collectors.toMap(PostListResponse::getId, Function.identity()));

        return ids.stream()
                .map(summaryMap::get)
                .filter(summary -> summary != null)
                .toList();
    }

    public PostNavigationResponse findMyPostNavigation(
            String communityCode,
            Long currentPostId,
            Long memberId,
            PostDetailContext detailContext,
            Long loginMemberId,
            boolean isAdmin
    ) {
        MyPostSearchCondition condition = detailContext.toMyPostSearchCondition();

        List<Long> orderedPostIds = postRepository.findOrderedMyPostIdsForNavigation(
                communityCode,
                memberId,
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachmentsOrNull(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSort()
        );

        return buildNavigationResponse(currentPostId, orderedPostIds, condition.getSafeSize(), loginMemberId, isAdmin);
    }

    public PostNavigationResponse findAdminPostNavigation(
            String communityCode,
            Long currentPostId,
            PostDetailContext detailContext,
            Long loginMemberId,
            boolean isAdmin
    ) {
        AdminPostSearchCondition condition = detailContext.toAdminPostSearchCondition();

        List<Long> orderedPostIds = postRepository.findOrderedAdminPostIdsForNavigation(
                communityCode,
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachments(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSortName()
        );

        return buildNavigationResponse(currentPostId, orderedPostIds, condition.getSafeSize(), loginMemberId, isAdmin);
    }


    private Page<PostListResponse> toSummaryPage(
            List<Long> orderedIds,
            Pageable pageable,
            long totalElements,
            Long loginMemberId,
            boolean isAdmin
    ) {
        if (orderedIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, totalElements);
        }

        Map<Long, PostListResponse> summaryMap = postRepository.findPostSummariesByIds(orderedIds)
                .stream()
                .map(post -> post.withSecretTitleVisible(loginMemberId, isAdmin))
                .collect(Collectors.toMap(PostListResponse::getId, Function.identity()));

        List<PostListResponse> content = orderedIds.stream()
                .map(summaryMap::get)
                .filter(summary -> summary != null)
                .toList();

        return new PageImpl<>(content, pageable, totalElements);
    }

    private PostNavigationResponse buildNavigationResponse(
            Long currentPostId,
            List<Long> orderedPostIds,
            int pageSize,
            Long loginMemberId,
            boolean isAdmin
    ) {
        if (orderedPostIds.isEmpty()) {
            return PostNavigationResponse.empty();
        }

        int currentIndex = orderedPostIds.indexOf(currentPostId);
        if (currentIndex < 0) {
            return PostNavigationResponse.empty();
        }

        Long previousPostId = currentIndex > 0 ? orderedPostIds.get(currentIndex - 1) : null;
        Long nextPostId = currentIndex < orderedPostIds.size() - 1 ? orderedPostIds.get(currentIndex + 1) : null;

        List<Long> targetIds = new ArrayList<>();
        if (previousPostId != null) {
            targetIds.add(previousPostId);
        }
        if (nextPostId != null) {
            targetIds.add(nextPostId);
        }

        if (targetIds.isEmpty()) {
            return PostNavigationResponse.empty();
        }

        Map<Long, PostListResponse> summaryMap = postRepository.findPostSummariesByIds(targetIds)
                .stream()
                .map(post -> post.withSecretTitleVisible(loginMemberId, isAdmin))
                .collect(Collectors.toMap(PostListResponse::getId, Function.identity()));

        PostNavigationItemResponse previousPost = null;
        if (previousPostId != null) {
            PostListResponse previousSummary = summaryMap.get(previousPostId);
            if (previousSummary != null) {
                int previousPage = ((currentIndex - 1) / pageSize) + 1;
                previousPost = PostNavigationItemResponse.of(
                        previousSummary.getId(),
                        previousPage,
                        previousSummary.getDisplayTitle()
                );
            }
        }

        PostNavigationItemResponse nextPost = null;
        if (nextPostId != null) {
            PostListResponse nextSummary = summaryMap.get(nextPostId);
            if (nextSummary != null) {
                int nextPage = ((currentIndex + 1) / pageSize) + 1;
                nextPost = PostNavigationItemResponse.of(
                        nextSummary.getId(),
                        nextPage,
                        nextSummary.getDisplayTitle()
                );
            }
        }

        return PostNavigationResponse.of(previousPost, nextPost);
    }
}
