package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.AdminLogResponse;
import com.gahih.domain.admin.dto.AdminLogSearchCondition;
import com.gahih.domain.admin.repository.AdminLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    public Page<AdminLogResponse> searchLogs(AdminLogSearchCondition condition) {
        return adminLogRepository.searchLogPage(
                condition,
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public List<String> findDistinctLogActions() {
        return adminLogRepository.findDistinctActions();
    }
}
