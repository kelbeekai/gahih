package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.dto.AdminLogResponse;
import com.gahih.domain.admin.dto.AdminLogSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminLogRepositoryCustom {

    Page<AdminLogResponse> searchLogPage(AdminLogSearchCondition condition, Pageable pageable);

    List<String> findDistinctActions();
}