package com.gahih.domain.admin.dto;

import com.gahih.domain.admin.enumtype.AdminNicknameHistorySearchType;
import com.gahih.domain.admin.enumtype.AdminNicknameHistorySortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminNicknameHistorySearchCondition {

    private AdminNicknameHistorySearchType searchType = AdminNicknameHistorySearchType.ALL;
    private String keyword;
    private String changeType;
    private AdminNicknameHistorySortType sort = AdminNicknameHistorySortType.LATEST;
    private int page = 1;
    private int size = 20;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public String getChangeTypeOrNull() {
        if (changeType == null || changeType.isBlank()) {
            return null;
        }
        return changeType.trim();
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

    public String getSortName() {
        return sort == null ? AdminNicknameHistorySortType.LATEST.name() : sort.name();
    }

    public String getSearchTypeName() {
        return searchType == null ? AdminNicknameHistorySearchType.ALL.name() : searchType.name();
    }
}