package com.gahih.domain.admin.repository;

import com.gahih.domain.admin.dto.AdminNicknameReservationResponse;
import com.gahih.domain.admin.dto.AdminNicknameReservationSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminNicknameReservationQueryRepository {

    Page<AdminNicknameReservationResponse> searchNicknameReservationPage(
            AdminNicknameReservationSearchCondition condition,
            Pageable pageable
    );
}