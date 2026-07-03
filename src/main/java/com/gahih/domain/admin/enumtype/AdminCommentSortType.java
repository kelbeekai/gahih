package com.gahih.domain.admin.enumtype;

public enum AdminCommentSortType {

    LATEST("최신순"),
    OLDEST("오래된순"),
    LIKE_COUNT("좋아요순"),
    DISLIKE_COUNT("싫어요순");

    private final String description;

    AdminCommentSortType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}