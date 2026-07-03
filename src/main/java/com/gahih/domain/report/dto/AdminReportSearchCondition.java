package com.gahih.domain.report.dto;

import com.gahih.domain.report.enumtype.AdminReportSearchType;
import com.gahih.domain.report.enumtype.AdminReportSortType;
import com.gahih.domain.report.enumtype.ReportStatus;
import com.gahih.domain.report.enumtype.ReportTargetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportSearchCondition {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    private String communityCode;
    private ReportTargetType targetType;
    private ReportStatus status = ReportStatus.PENDING;
    private AdminReportSearchType searchType = AdminReportSearchType.TARGET_NAME;
    private String keyword;
    private AdminReportSortType sort = AdminReportSortType.PENDING_OLDEST;
    private Integer page = DEFAULT_PAGE;
    private Integer size = DEFAULT_SIZE;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public int getSafePage() {
        return (page == null || page < 1) ? DEFAULT_PAGE : page;
    }

    public int getSafeSize() {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size == 20 || size == 40 || size == 60) {
            return size;
        }
        return DEFAULT_SIZE;
    }

    public String getSortName() {
        return sort != null ? sort.name() : AdminReportSortType.PENDING_OLDEST.name();
    }
}