package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminLogPeriodType {
    ALL("전체"),
    TODAY("오늘"),
    LAST_7_DAYS("최근 7일"),
    LAST_30_DAYS("최근 30일"),
    CUSTOM("직접 지정");

    private final String displayName;

    AdminLogPeriodType(String displayName) {
        this.displayName = displayName;
    }
}