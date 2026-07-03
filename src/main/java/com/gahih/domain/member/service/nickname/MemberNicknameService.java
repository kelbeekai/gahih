package com.gahih.domain.member.service;

import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.entity.NicknameHistory;
import com.gahih.domain.member.entity.NicknameReservation;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.repository.NicknameHistoryRepository;
import com.gahih.domain.member.repository.NicknameReservationRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNicknameService {

    private static final long NICKNAME_REUSE_BLOCK_DAYS = 90;
    private static final long FINALIZED_MEMBER_HISTORY_RETENTION_DAYS = 90;

    private final MemberRepository memberRepository;
    private final NicknameHistoryRepository nicknameHistoryRepository;
    private final NicknameReservationRepository nicknameReservationRepository;
    private final AdminLogRepository adminLogRepository;

    @Transactional
    public int deleteExpiredNicknameReservations() {
        List<NicknameReservation> expiredReservations =
                nicknameReservationRepository.findAllByExpiresAtBefore(LocalDateTime.now());

        int deletedCount = expiredReservations.size();
        if (deletedCount == 0) {
            return 0;
        }

        nicknameReservationRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        return deletedCount;
    }

    @Transactional
    public int anonymizeExpiredDeletedMemberHistories() {
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(FINALIZED_MEMBER_HISTORY_RETENTION_DAYS);

        List<Member> expiredDeletedMembers =
                memberRepository.findAllByStatusAndFinalizedAtBefore(MemberStatus.DELETED, cutoffDateTime);

        int anonymizedCount = 0;

        for (Member member : expiredDeletedMembers) {
            anonymizeNicknameHistories(member.getId());
            anonymizeAdminLogs(member.getId());
            anonymizedCount++;
        }

        return anonymizedCount;
    }

    @Transactional
    public void forceChangeNicknameByAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (member.isDeleted()) {
            throw new BusinessException("이미 최종 종료된 회원입니다.");
        }

        String previousNickname = member.getNickname();
        String forcedNickname = generateForcedNickname();

        // 닉네임 변경 (쿨타임 무시)
        member.forceChangeNickname(forcedNickname);

        // 이력 저장
        saveAdminForcedNicknameHistory(member, previousNickname, forcedNickname);

        // reservation 저장 (이전 닉네임)
        saveNicknameReservationForAdminForced(previousNickname);
    }

    private void saveAdminForcedNicknameHistory(Member member, String previousNickname, String newNickname) {
        NicknameHistory nicknameHistory = NicknameHistory.createAdminForced(member, previousNickname, newNickname);
        nicknameHistoryRepository.save(nicknameHistory);
    }

    private void saveNicknameReservationForAdminForced(String nickname) {
        NicknameReservation nicknameReservation =
                NicknameReservation.reserveForAdminForcedNickname(nickname, NICKNAME_REUSE_BLOCK_DAYS);
        nicknameReservationRepository.save(nicknameReservation);
    }

    private void anonymizeNicknameHistories(Long memberId) {
        List<NicknameHistory> histories = nicknameHistoryRepository.findAllByMemberId(memberId);

        for (NicknameHistory history : histories) {
            history.anonymizeNicknameFields();
        }
    }

    private void anonymizeAdminLogs(Long memberId) {
        List<AdminLog> logs =
                adminLogRepository.findAllByTargetTypeAndTargetId(AdminLogTargetType.MEMBER, memberId);

        for (AdminLog log : logs) {
            log.anonymizeMemberNicknameData(null);
        }
    }

    private String generateForcedNickname() {
        for (int i = 0; i < 20; i++) {
            String random = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            String candidate = "user_" + random;

            boolean duplicated = memberRepository.existsByNickname(candidate);
            boolean reserved = nicknameReservationRepository
                    .existsByNicknameIgnoreCaseAndExpiresAtAfter(candidate, LocalDateTime.now());

            if (!duplicated && !reserved) {
                return candidate;
            }
        }

        throw new BusinessException("강제 변경용 닉네임 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
}
