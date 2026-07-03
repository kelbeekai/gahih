package com.gahih.domain.admin.dto;

import com.gahih.domain.admin.enumtype.AdminMemberSearchType;
import com.gahih.domain.admin.enumtype.AdminMemberSortType;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberSearchCondition {

    private AdminMemberSearchType searchType = AdminMemberSearchType.ALL;
    private String keyword;
    private MemberRole role;
    private MemberStatus status;
    private AdminMemberSortType sort = AdminMemberSortType.LATEST;
    private int page = 1;
    private int size = 20;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
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
        if (sort == null) {
            return AdminMemberSortType.LATEST.name();
        }
        return sort.name();
    }

    public String getSearchTypeName() {
        if (searchType == null) {
            return AdminMemberSearchType.ALL.name();
        }
        return searchType.name();
    }
}
