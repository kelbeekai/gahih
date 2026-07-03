package com.gahih.domain.report.enumtype;

public enum AdminReportSortType {
    PENDING_OLDEST("조치 전 오래된순"),
    LAST_REPORTED_DESC("최근 신고순"),
    REPORT_COUNT_DESC("신고 많은순");

    private final String description;

    AdminReportSortType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}