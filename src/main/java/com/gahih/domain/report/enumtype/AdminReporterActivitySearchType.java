package com.gahih.domain.report.enumtype;

public enum AdminReporterActivitySearchType {
    ALL("전체"),
    MEMBER_ID("회원 ID"),
    USERNAME("아이디"),
    NICKNAME("닉네임");

    private final String description;

    AdminReporterActivitySearchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}