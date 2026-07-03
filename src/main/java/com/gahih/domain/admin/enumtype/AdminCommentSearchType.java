package com.gahih.domain.admin.enumtype;

public enum AdminCommentSearchType {
    CONTENT("댓글 내용"),
    WRITER_NICKNAME("작성자 닉네임"),
    POST_TITLE("게시글 제목"),
    POST_ID("게시글 ID"),
    COMMENT_ID("댓글 ID");

    private final String description;

    AdminCommentSearchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}