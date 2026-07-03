package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.dto.AdminNicknameHistoryResponse;
import com.gahih.domain.admin.dto.AdminNicknameHistorySearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminNicknameHistoryQueryRepository {

    Page<AdminNicknameHistoryResponse> searchNicknameHistoryPage(
            AdminNicknameHistorySearchCondition condition,
            Pageable pageable
    );
}