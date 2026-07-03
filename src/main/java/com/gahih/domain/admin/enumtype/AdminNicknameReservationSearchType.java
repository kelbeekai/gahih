package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminNicknameReservationSearchType {
    ALL("전체"),
    NICKNAME("닉네임"),
    REASON_TYPE("사유");

    private final String displayName;

    AdminNicknameReservationSearchType(String displayName) {
        this.displayName = displayName;
    }
}