package com.gahih.domain.admin.dto;

import com.gahih.domain.admin.enumtype.AdminNicknameReservationSearchType;
import com.gahih.domain.admin.enumtype.AdminNicknameReservationSortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminNicknameReservationSearchCondition {

    private AdminNicknameReservationSearchType searchType = AdminNicknameReservationSearchType.ALL;
    private String keyword;
    private String reasonType;
    private AdminNicknameReservationSortType sort = AdminNicknameReservationSortType.EXPIRES_SOON;
    private int page = 1;
    private int size = 20;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public String getReasonTypeOrNull() {
        if (reasonType == null || reasonType.isBlank()) {
            return null;
        }
        return reasonType.trim();
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
        return sort == null ? AdminNicknameReservationSortType.EXPIRES_SOON.name() : sort.name();
    }

    public String getSearchTypeName() {
        return searchType == null ? AdminNicknameReservationSearchType.ALL.name() : searchType.name();
    }
}