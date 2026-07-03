package com.gahih.domain.member.service.status;

import com.gahih.domain.member.service.nickname.MemberNicknameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawnMemberCleanupScheduler {

    private final MemberStatusService memberStatusService;
    private final MemberNicknameService memberNicknameService;

    /**
     * 운영 기본값: 1분마다
     * 1) 만료된 탈퇴 유예 회원 최종 종료
     * 2) 만료된 닉네임 reservation 정리
     */
    @Scheduled(fixedDelay = 60_000)
    // @Scheduled(fixedDelay = 10_000) // 빠른 테스트용
    public void cleanupMemberRelatedExpiredData() {
        int finalizedCount = memberStatusService.finalizeExpiredWithdrawnMembers();
        int deletedReservationCount = memberNicknameService.deleteExpiredNicknameReservations();
        int anonymizedDeletedMemberCount = memberNicknameService.anonymizeExpiredDeletedMemberHistories();

        if (finalizedCount > 0) {
            log.info("[withdrawn-cleanup] finalizedCount={}", finalizedCount);
        }

        if (deletedReservationCount > 0) {
            log.info("[nickname-reservation-cleanup] deletedReservationCount={}", deletedReservationCount);
        }

        if (anonymizedDeletedMemberCount > 0) {
            log.info("[deleted-member-history-anonymize] anonymizedCount={}", anonymizedDeletedMemberCount);
        }
    }
}