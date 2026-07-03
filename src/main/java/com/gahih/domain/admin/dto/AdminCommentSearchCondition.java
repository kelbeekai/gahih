package com.gahih.domain.admin.dto;

import com.gahih.domain.admin.enumtype.AdminCommentSearchType;
import com.gahih.domain.admin.enumtype.AdminCommentSortType;
import com.gahih.domain.comment.enumtype.CommentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCommentSearchCondition {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    private String communityCode;
    private AdminCommentSearchType searchType = AdminCommentSearchType.CONTENT;
    private String keyword;
    private CommentStatus status;
    private AdminCommentSortType sort = AdminCommentSortType.LATEST;
    private Integer page = DEFAULT_PAGE;
    private Integer size = DEFAULT_SIZE;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public int getSafePage() {
        return (page == null || page < 1) ? DEFAULT_PAGE : page;
    }

    public int getSafeSize() {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size == 20 || size == 40 || size == 60) {
            return size;
        }
        return DEFAULT_SIZE;
    }

    public String getSortName() {
        return sort != null ? sort.name() : AdminCommentSortType.LATEST.name();
    }
}