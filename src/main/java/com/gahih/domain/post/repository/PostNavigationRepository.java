package com.gahih.domain.post.repository;

import com.gahih.domain.post.dto.PostListResponse;
import com.gahih.domain.post.enumtype.PostSortType;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;

import java.util.List;

public interface PostNavigationRepository {

    List<Long> findOrderedPostIdsForNavigation(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            PostSortType sortType
    );

    List<PostListResponse> findPostSummariesByIds(List<Long> postIds);
}
