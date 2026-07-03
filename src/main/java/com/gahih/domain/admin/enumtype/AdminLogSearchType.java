package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminLogSearchType {

    ALL("전체"),
    ADMIN_USERNAME("관리자 아이디"),
    ADMIN_NICKNAME("관리자 닉네임"),
    ACTION("로그 타입"),
    TARGET_ID("대상 ID"),
    TARGET_NAME("대상 이름"),
    REASON("사유");

    private final String displayName;

    AdminLogSearchType(String displayName) {
        this.displayName = displayName;
    }

}