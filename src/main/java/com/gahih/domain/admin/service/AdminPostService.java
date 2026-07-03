package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.AdminPostResponse;
import com.gahih.domain.admin.dto.AdminPostSearchCondition;
import com.gahih.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostQueryService {

    private static final int MAX_PINNED_POST_COUNT = 5;

    private final PostRepository postRepository;

    public Page<AdminPostResponse> searchPosts(AdminPostSearchCondition condition) {
        return postRepository.searchAdminPostPage(
                condition.getCommunityCode(),
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.isOnlyWithAttachments(),
                condition.getSortName(),
                PageRequest.of(condition.getSafePage() - 1, /*ADMIN_POST_PAGE_SIZE*/ condition.getSafeSize())
        );
    }

    public List<AdminPostResponse> findPinnedPosts(String communityCode, Long categoryId) {
        int limit = (categoryId == null) ? Integer.MAX_VALUE : MAX_PINNED_POST_COUNT;
        return postRepository.findPinnedAdminPostList(communityCode, categoryId, limit);
    }
}
