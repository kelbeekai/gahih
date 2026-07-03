package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminNicknameHistorySortType {
    LATEST("최신순"),
    OLDEST("오래된 순");

    private final String displayName;

    AdminNicknameHistorySortType(String displayName) {
        this.displayName = displayName;
    }
}