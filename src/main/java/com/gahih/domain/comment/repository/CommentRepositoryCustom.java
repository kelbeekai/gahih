package com.gahih.domain.comment.repository;

import com.gahih.domain.admin.dto.AdminCommentResponse;
import com.gahih.domain.admin.enumtype.AdminCommentSearchType;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.member.dto.MyRecentInteractionCandidateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepositoryCustom {

    Page<AdminCommentResponse> searchAdminCommentPage(
            String communityCode,
            String keyword,
            AdminCommentSearchType searchType,
            CommentStatus status,
            String sortName,
            Pageable pageable
    );

    Page<Comment> searchCommentPage(
            Long postId,
            CommentSortType sortType,
            Pageable pageable
    );

    List<MyRecentInteractionCandidateResponse> findRecentCommentInteractionPosts(
            Long postOwnerId,
            LocalDateTime since,
            int limit
    );

    List<MyRecentInteractionCandidateResponse> findRecentCommentInteractionPosts(
            String communityCode,
            Long postOwnerId,
            LocalDateTime since,
            int limit
    );
}