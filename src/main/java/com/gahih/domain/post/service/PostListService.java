package com.gahih.domain.post.service;

import com.gahih.domain.post.dto.PostListResponse;
import com.gahih.domain.post.dto.PostSearchCondition;
import com.gahih.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostListService {

    private final PostRepository postRepository;

    private static final int MAX_PINNED_POST_COUNT = 5;

    /**
     * 페이지네이션 적용 이전 메서드
     */
    public List<PostListResponse> findAllPosts() {
        return postRepository.findAllByOrderByIdDesc()
                .stream()
                .map(PostListResponse::from)
                .toList();
    }

    public Page<PostListResponse> searchPosts(PostSearchCondition condition, Long loginMemberId, boolean isAdmin) {
        Page<PostListResponse> page = postRepository.searchPostPage(
                condition.getCommunityCode(),
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachmentsOrNull(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSort(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );

        List<PostListResponse> content = page.getContent().stream()
                .map(post -> post.withSecretTitleVisible(loginMemberId, isAdmin))
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    public List<PostListResponse> findPinnedPosts(
            String communityCode,
            Long categoryId,
            Long loginMemberId,
            boolean isAdmin
    ) {
        PostSearchCondition condition = new PostSearchCondition();
        condition.setCommunityCode(communityCode);
        condition.setCategoryId(categoryId);

        return findPinnedPosts(condition, loginMemberId, isAdmin);
    }

    public List<PostListResponse> findPinnedPosts(
            PostSearchCondition condition,
            Long loginMemberId,
            boolean isAdmin
    ) {
        int limit = (condition.getCategoryId() == null) ? Integer.MAX_VALUE : MAX_PINNED_POST_COUNT;
        return postRepository.findPinnedPostList(
                        condition.getCommunityCode(),
                        condition.getCategoryId(),
                        condition.getOnlyWithAttachmentsOrNull(),
                        condition.getTradeType(),
                        condition.getTradeStatus(),
                        limit
                )
                .stream()
                .map(post -> post.withSecretTitleVisible(loginMemberId, isAdmin))
                .toList();
    }
}
