package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.AdminNicknameHistoryResponse;
import com.gahih.domain.admin.dto.AdminNicknameHistorySearchCondition;
import com.gahih.domain.admin.dto.AdminNicknameReservationResponse;
import com.gahih.domain.admin.dto.AdminNicknameReservationSearchCondition;
import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.admin.repository.AdminNicknameHistoryQueryRepository;
import com.gahih.domain.admin.repository.AdminNicknameReservationQueryRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.service.nickname.MemberNicknameService;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNicknameService {

    private final AdminNicknameHistoryQueryRepository adminNicknameHistoryQueryRepository;
    private final AdminNicknameReservationQueryRepository adminNicknameReservationQueryRepository;

    private final MemberRepository memberRepository;
    private final MemberNicknameService memberNicknameService;
    private final AdminLogRepository adminLogRepository;

    public Page<AdminNicknameHistoryResponse> searchNicknameHistories(AdminNicknameHistorySearchCondition condition) {
        return adminNicknameHistoryQueryRepository.searchNicknameHistoryPage(
                condition,
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public Page<AdminNicknameReservationResponse> searchNicknameReservations(
            AdminNicknameReservationSearchCondition condition
    ) {
        return adminNicknameReservationQueryRepository.searchNicknameReservationPage(
                condition,
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    @Transactional
    public void forceChangeMemberNickname(Long adminId, Long targetMemberId, String reason) {
        Member admin = getAdmin(adminId);

        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("대상 회원이 존재하지 않습니다."));

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException("관리자 본인의 닉네임은 강제로 변경할 수 없습니다.");
        }

        String beforeSnapshot = buildMemberSnapshot(target);

        memberNicknameService.forceChangeNicknameByAdmin(targetMemberId);

        String afterSnapshot = buildMemberSnapshot(target);

        String normalizedReason = requireReason(reason, "강제 닉네임 변경 사유");

        saveLog(
                admin,
                "FORCE_CHANGE_NICKNAME",
                AdminLogTargetType.MEMBER,
                target.getId(),
                target.getNickname(),
                null,
                null,
                normalizedReason,
                beforeSnapshot,
                afterSnapshot
        );
    }

    private Member getAdmin(Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 관리자입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new UnauthorizedException("관리자 권한이 없습니다.");
        }

        return admin;
    }

    private void saveLog(
            Member admin,
            String action,
            AdminLogTargetType targetType,
            Long targetId,
            String targetName,
            String targetCommunityCode,
            String targetCommunityName,
            String reason,
            String beforeSnapshot,
            String afterSnapshot
    ) {
        AdminLog adminLog = AdminLog.create(
                admin,
                action,
                targetType,
                targetId,
                targetName,
                reason,
                beforeSnapshot,
                afterSnapshot,
                targetCommunityCode,
                targetCommunityName
        );
        adminLogRepository.save(adminLog);

        log.info(
                "Admin action completed. action={}, adminId={}, targetType={}, targetId={}, communityCode={}",
                action,
                admin.getId(),
                targetType,
                targetId,
                targetCommunityCode
        );
    }

    private String buildMemberSnapshot(Member member) {
        return "username=" + member.getUsername()
                + ", nickname=" + member.getNickname()
                + ", email=" + member.getEmail()
                + ", role=" + member.getRole().name()
                + ", status=" + member.getStatus().name();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }

    private String requireReason(String reason, String label) {
        String normalized = normalizeReason(reason);
        if (normalized == null) {
            throw new BusinessException(label + "를 입력해주세요.");
        }
        return normalized;
    }
}
