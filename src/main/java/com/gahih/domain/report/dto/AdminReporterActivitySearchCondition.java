package com.gahih.domain.report.dto;

import com.gahih.domain.report.enumtype.AdminReporterActivitySearchType;
import com.gahih.domain.report.enumtype.AdminReporterActivitySortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReporterActivitySearchCondition {

    private AdminReporterActivitySearchType searchType = AdminReporterActivitySearchType.ALL;
    private String keyword;
    private AdminReporterActivitySortType sort = AdminReporterActivitySortType.TOTAL_REPORT_DESC;
    private Integer size = 20;
    private Integer page = 1;
    private String communityCode;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public int getSafeSize() {
        if (size == null) {
            return 20;
        }
        if (size != 20 && size != 40 && size != 60) {
            return 20;
        }
        return size;
    }

    public int getSafePage() {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public String getSortName() {
        return sort == null ? AdminReporterActivitySortType.TOTAL_REPORT_DESC.name() : sort.name();
    }

    public String getSearchTypeName() {
        return searchType == null ? AdminReporterActivitySearchType.ALL.name() : searchType.name();
    }

    public String getCommunityCodeOrNull() {
        if (communityCode == null || communityCode.isBlank()) {
            return null;
        }
        return communityCode.trim().toUpperCase();
    }
}