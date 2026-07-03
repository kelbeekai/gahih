package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminNicknameReservationSortType {
    EXPIRES_SOON("만료 임박순"),
    LATEST("최신 생성순"),
    OLDEST("오래된 순");

    private final String displayName;

    AdminNicknameReservationSortType(String displayName) {
        this.displayName = displayName;
    }
}