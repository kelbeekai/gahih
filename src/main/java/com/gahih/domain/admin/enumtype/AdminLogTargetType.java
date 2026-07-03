package com.gahih.domain.admin.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminLogTargetType {

    MEMBER("회원"),
    POST("게시글"),
    COMMENT("댓글"),
    ATTACHMENT("첨부파일");

    private final String displayName;
}