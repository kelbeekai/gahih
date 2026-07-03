package com.gahih.domain.comment.enumtype;

import lombok.Getter;

@Getter
public enum CommentStatus {

    ACTIVE("정상"),
    USER_DELETED("작성자 삭제"),
    ADMIN_BLINDED("블라인드 처리됨"),
    ADMIN_DELETED("관리자 삭제");

    private final String displayName;

    CommentStatus(String displayName) {
        this.displayName = displayName;
    }

}