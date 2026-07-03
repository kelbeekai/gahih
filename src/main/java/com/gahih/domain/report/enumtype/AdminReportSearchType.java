package com.gahih.domain.report.enumtype;

public enum AdminReportSearchType {
    TARGET_NAME("대상명"),
    WRITER_NICKNAME("작성자"),
    TARGET_ID("대상 ID"),
    PARENT_POST_TITLE("상위 게시글 제목");

    private final String description;

    AdminReportSearchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}