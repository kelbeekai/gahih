package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminNicknameHistorySearchType {
    ALL("전체"),
    MEMBER_ID("회원 ID"),
    CURRENT_NICKNAME("현재 닉네임"),
    PREVIOUS_NICKNAME("이전 닉네임"),
    NEW_NICKNAME("변경 후 닉네임");

    private final String displayName;

    AdminNicknameHistorySearchType(String displayName) {
        this.displayName = displayName;
    }
}