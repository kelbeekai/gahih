package com.gahih.domain.report.enumtype;

public enum ReportTargetType {

    MEMBER("회원"),
    POST("게시글"),
    COMMENT("댓글"),
    ATTACHMENT("첨부파일");

    private final String description;

    ReportTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}