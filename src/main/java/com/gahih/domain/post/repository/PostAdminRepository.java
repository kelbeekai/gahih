package com.gahih.domain.post.repository;

import com.gahih.domain.admin.dto.AdminPostResponse;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostAdminRepository {

    // 관리자용 QueryDSL 메서드
    Page<AdminPostResponse> searchAdminPostPage(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            String sort,
            Pageable pageable
    );

    List<AdminPostResponse> findPinnedAdminPostList(
            String communityCode,
            Long categoryId,
            TradeType tradeType,
            TradeStatus tradeStatus,
            int limit
    );

    List<Long> findOrderedAdminPostIdsForNavigation(
            String communityCode,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            String sort
    );
}
