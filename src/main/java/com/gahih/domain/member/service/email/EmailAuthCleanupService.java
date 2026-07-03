package com.gahih.domain.member.service.email;

import com.gahih.domain.member.repository.EmailAuthRequestRepository;
import com.gahih.domain.member.repository.PasswordResetSessionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailAuthCleanupService {

    private final EmailAuthRequestRepository emailAuthRequestRepository;
    private final PasswordResetSessionRepository passwordResetSessionRepository;

    /**
     * 인증 성공 후 최종 회원가입/이메일 변경/비밀번호 재설정 흐름이 이어질 수 있도록
     * verifiedAt, usedAt 직후 즉시 삭제하지 않고 짧은 유예 시간을 둔다.
     *
     * 운영 기본 추천: 30분
     * 더 짧게 운영하고 싶으면 10분
     * 장애 분석을 더 중시하면 1일(1440분)까지 늘릴 수 있다.
     */
    @Value("${app.email-auth.cleanup.verified-retention-minutes:30}")
    private long verifiedRetentionMinutes;

    @Value("${app.email-auth.cleanup.used-reset-session-retention-minutes:30}")
    private long usedResetSessionRetentionMinutes;

    /**
     * 안전망 보관 기간.
     * 어떤 이유로 expiresAt/verifiedAt/usedAt 기준 삭제에서 빠진 오래된 임시 데이터를 제거한다.
     *
     * 개인정보 최소 보관 기준 추천: 24시간
     * 장애 분석용 일 단위 보관이 필요하면 72~168시간으로 조정 가능.
     */
    @Value("${app.email-auth.cleanup.max-retention-hours:24}")
    private long maxRetentionHours;

    @Transactional
    public CleanupResult cleanupExpiredAuthenticationData() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime verifiedRetentionCutoff = now.minusMinutes(verifiedRetentionMinutes);
        LocalDateTime usedResetSessionRetentionCutoff = now.minusMinutes(usedResetSessionRetentionMinutes);
        LocalDateTime createdRetentionCutoff = now.minusHours(maxRetentionHours);

        int expiredEmailAuthRequestCount = emailAuthRequestRepository.deleteAllExpired(now);
        int verifiedEmailAuthRequestCount =
                emailAuthRequestRepository.deleteAllVerifiedBefore(verifiedRetentionCutoff);
        int oldEmailAuthRequestCount =
                emailAuthRequestRepository.deleteAllCreatedBefore(createdRetentionCutoff);

        int expiredPasswordResetSessionCount = passwordResetSessionRepository.deleteAllExpired(now);
        int usedPasswordResetSessionCount =
                passwordResetSessionRepository.deleteAllUsedBefore(usedResetSessionRetentionCutoff);
        int oldPasswordResetSessionCount =
                passwordResetSessionRepository.deleteAllCreatedBefore(createdRetentionCutoff);

        return CleanupResult.of(
                expiredEmailAuthRequestCount,
                verifiedEmailAuthRequestCount,
                oldEmailAuthRequestCount,
                expiredPasswordResetSessionCount,
                usedPasswordResetSessionCount,
                oldPasswordResetSessionCount
        );
    }

    @Getter
    public static class CleanupResult {

        private final int expiredEmailAuthRequestCount;
        private final int verifiedEmailAuthRequestCount;
        private final int oldEmailAuthRequestCount;
        private final int expiredPasswordResetSessionCount;
        private final int usedPasswordResetSessionCount;
        private final int oldPasswordResetSessionCount;

        private CleanupResult(
                int expiredEmailAuthRequestCount,
                int verifiedEmailAuthRequestCount,
                int oldEmailAuthRequestCount,
                int expiredPasswordResetSessionCount,
                int usedPasswordResetSessionCount,
                int oldPasswordResetSessionCount
        ) {
            this.expiredEmailAuthRequestCount = expiredEmailAuthRequestCount;
            this.verifiedEmailAuthRequestCount = verifiedEmailAuthRequestCount;
            this.oldEmailAuthRequestCount = oldEmailAuthRequestCount;
            this.expiredPasswordResetSessionCount = expiredPasswordResetSessionCount;
            this.usedPasswordResetSessionCount = usedPasswordResetSessionCount;
            this.oldPasswordResetSessionCount = oldPasswordResetSessionCount;
        }

        public static CleanupResult of(
                int expiredEmailAuthRequestCount,
                int verifiedEmailAuthRequestCount,
                int oldEmailAuthRequestCount,
                int expiredPasswordResetSessionCount,
                int usedPasswordResetSessionCount,
                int oldPasswordResetSessionCount
        ) {
            return new CleanupResult(
                    expiredEmailAuthRequestCount,
                    verifiedEmailAuthRequestCount,
                    oldEmailAuthRequestCount,
                    expiredPasswordResetSessionCount,
                    usedPasswordResetSessionCount,
                    oldPasswordResetSessionCount
            );
        }

        public int getTotalDeletedCount() {
            return expiredEmailAuthRequestCount
                    + verifiedEmailAuthRequestCount
                    + oldEmailAuthRequestCount
                    + expiredPasswordResetSessionCount
                    + usedPasswordResetSessionCount
                    + oldPasswordResetSessionCount;
        }
    }
}