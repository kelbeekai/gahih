package com.gahih.domain.admin.dto;

import com.gahih.domain.admin.enumtype.AdminLogPeriodType;
import com.gahih.domain.admin.enumtype.AdminLogSearchType;
import com.gahih.domain.admin.enumtype.AdminLogSortType;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdminLogSearchCondition {

    private AdminLogSearchType searchType = AdminLogSearchType.ALL;
    private String keyword;
    private String action;
    private AdminLogTargetType targetType;
    private AdminLogPeriodType period = AdminLogPeriodType.ALL;
    private String targetCommunityCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private AdminLogSortType sort = AdminLogSortType.LATEST;
    private int page = 1;
    private int size = 20;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public String getActionOrNull() {
        if (action == null || action.isBlank()) {
            return null;
        }
        return action.trim();
    }

    public AdminLogTargetType getTargetTypeOrNull() {
        return targetType;
    }

    public AdminLogPeriodType getPeriodOrDefault() {
        return period == null ? AdminLogPeriodType.ALL : period;
    }

    public LocalDateTime getStartDateTimeOrNull() {
        return switch (getPeriodOrDefault()) {
            case TODAY -> LocalDate.now().atStartOfDay();
            case LAST_7_DAYS -> LocalDate.now().minusDays(6).atStartOfDay();
            case LAST_30_DAYS -> LocalDate.now().minusDays(29).atStartOfDay();
            case CUSTOM -> {
                LocalDate resolved = getResolvedCustomStartDate();
                yield resolved == null ? null : resolved.atStartOfDay();
            }
            case ALL -> null;
        };
    }

    public LocalDateTime getEndDateTimeExclusiveOrNull() {
        return switch (getPeriodOrDefault()) {
            case TODAY, LAST_7_DAYS, LAST_30_DAYS -> LocalDate.now().plusDays(1).atStartOfDay();
            case CUSTOM -> {
                LocalDate resolved = getResolvedCustomEndDate();
                yield resolved == null ? null : resolved.plusDays(1).atStartOfDay();
            }
            case ALL -> null;
        };
    }

    public boolean isCustomPeriod() {
        return getPeriodOrDefault() == AdminLogPeriodType.CUSTOM;
    }

    private LocalDate getResolvedCustomStartDate() {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return startDate;
        }
        return startDate.isAfter(endDate) ? endDate : startDate;
    }

    private LocalDate getResolvedCustomEndDate() {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return startDate;
        }
        return startDate.isAfter(endDate) ? startDate : endDate;
    }

    public int getSafePage() {
        return Math.max(page, 1);
    }

    public int getSafeSize() {
        return switch (size) {
            case 40, 60 -> size;
            default -> 20;
        };
    }

    public String getSearchTypeName() {
        if (searchType == null) {
            return AdminLogSearchType.ALL.name();
        }
        return searchType.name();
    }

    public String getSortName() {
        if (sort == null) {
            return AdminLogSortType.LATEST.name();
        }
        return sort.name();
    }

    public String getTargetCommunityCodeOrNull() {
        if (targetCommunityCode == null || targetCommunityCode.isBlank()) {
            return null;
        }
        return targetCommunityCode.trim().toUpperCase();
    }
}