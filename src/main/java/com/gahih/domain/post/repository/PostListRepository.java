package com.gahih.domain.post.repository;

import com.gahih.domain.post.dto.PostListResponse;
import com.gahih.domain.post.enumtype.PostSortType;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostListRepository {

    // 일반 게시글 목록용 QueryDSL 메서드
    Page<PostListResponse> searchPostPage(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            PostSortType sortType,
            Pageable pageable
    );

    List<PostListResponse> findPinnedPostList(
            String communityCode,
            Long categoryId,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            int limit
    );
}
