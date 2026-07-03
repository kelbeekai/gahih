package com.gahih.domain.post.repository;

import com.gahih.domain.member.dto.MyPostListResponse;
import com.gahih.domain.member.enumtype.MyPostSortType;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostMyPageRepository {

    // 마이페이지 게시글 목록용 QueryDSL 메서드
    Page<MyPostListResponse> searchMyPostPage(
            String communityCode,
            Long memberId,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            MyPostSortType sortType,
            Pageable pageable
    );

    List<Long> findOrderedMyPostIdsForNavigation(
            String communityCode,
            Long memberId,
            Long categoryId,
            String keyword,
            Boolean secret,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            MyPostSortType sortType
    );
}