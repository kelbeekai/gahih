package com.gahih.domain.member.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthCleanupScheduler {

    private final EmailAuthCleanupService emailAuthCleanupService;

    /**
     * 운영 기본값: 10분마다 임시 인증/복구 데이터 정리.
     *
     * 개인정보 최소 보관 관점:
     * - 만료된 인증 요청은 삭제
     * - 인증 완료 후 30분 지난 요청은 삭제
     * - 사용 완료 후 30분 지난 재설정 세션은 삭제
     * - 생성 후 24시간 지난 임시 데이터는 안전망으로 삭제
     *
     * 장애 분석을 위해 일 단위 보관이 필요하면
     * application.properties의 app.email-auth.cleanup.* 값만 조정하면 된다.
     */
    @Scheduled(fixedDelay = 600_000)
    // @Scheduled(fixedDelay = 60_000) // 개발/테스트용: 1분마다
    // @Scheduled(fixedDelay = 10_000) // 빠른 테스트용: 10초마다
    public void cleanupExpiredAuthenticationData() {
        EmailAuthCleanupService.CleanupResult result =
                emailAuthCleanupService.cleanupExpiredAuthenticationData();

        if (result.getTotalDeletedCount() > 0) {
            log.info(
                    "[email-auth-cleanup] expiredEmailAuth={}, verifiedEmailAuth={}, oldEmailAuth={}, " +
                            "expiredPasswordResetSession={}, usedPasswordResetSession={}, oldPasswordResetSession={}, total={}",
                    result.getExpiredEmailAuthRequestCount(),
                    result.getVerifiedEmailAuthRequestCount(),
                    result.getOldEmailAuthRequestCount(),
                    result.getExpiredPasswordResetSessionCount(),
                    result.getUsedPasswordResetSessionCount(),
                    result.getOldPasswordResetSessionCount(),
                    result.getTotalDeletedCount()
            );
        }
    }
}