package com.gahih.domain.member.repository;

import com.gahih.domain.admin.dto.AdminMemberResponse;
import com.gahih.domain.admin.enumtype.AdminMemberSearchType;
import com.gahih.domain.admin.enumtype.AdminMemberSortType;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<AdminMemberResponse> searchAdminMemberPage(
            String keyword,
            AdminMemberSearchType searchType,
            MemberRole role,
            MemberStatus status,
            String sort,
            Pageable pageable
    );
}
