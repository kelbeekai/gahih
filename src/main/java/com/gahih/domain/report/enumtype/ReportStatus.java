package com.gahih.domain.report.enumtype;

public enum ReportStatus {

    PENDING("조치 전"),
    ACTION_TAKEN("조치 완료"),
    NO_ACTION_NEEDED("조치 필요 없음");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}