package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long>, AdminLogRepositoryCustom {

    List<AdminLog> findAllByOrderByIdDesc();

    List<AdminLog> findAllByTargetTypeAndTargetId(AdminLogTargetType targetType, Long targetId);
}