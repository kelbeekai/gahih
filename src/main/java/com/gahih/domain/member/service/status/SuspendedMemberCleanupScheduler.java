package com.gahih.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuspendedMemberCleanupScheduler {

    private final MemberStatusService memberStatusService;

    /**
     * 운영 기본값: 1분마다 만료된 기간정지 회원 자동 해제
     */
    @Scheduled(fixedDelay = 60_000)
    // @Scheduled(fixedDelay = 10_000) // 빠른 테스트용
    public void releaseExpiredSuspendedMembers() {
        int releasedCount = memberStatusService.releaseExpiredSuspendedMembers();

        if (releasedCount > 0) {
            log.info("[suspended-cleanup] releasedCount={}", releasedCount);
        }
    }
}