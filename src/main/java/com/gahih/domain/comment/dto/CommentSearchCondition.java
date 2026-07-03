package com.gahih.domain.comment.dto;

import com.gahih.domain.comment.enumtype.CommentSortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentSearchCondition {

    private static final int DEFAULT_SIZE = 20;

    private CommentSortType sort = CommentSortType.LIKE_COUNT;
    private int page = 1;
    private int size = DEFAULT_SIZE;

    public int getSafePage() {
        return Math.max(page, 1);
    }

    public int getSafeSize() {
        return DEFAULT_SIZE;
    }
}