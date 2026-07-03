package com.gahih.domain.member.service.email;

import com.gahih.domain.member.entity.EmailAuthRequest;
import com.gahih.domain.member.entity.PasswordResetSession;
import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import com.gahih.domain.member.repository.EmailAuthRequestRepository;
import com.gahih.domain.member.repository.PasswordResetSessionRepository;
import com.gahih.global.exception.DomainValidationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailAuthService {

    private final EmailAuthRequestRepository emailAuthRequestRepository;
    private final PasswordResetSessionRepository passwordResetSessionRepository;
    private final EmailSender emailSender;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final VerificationCodeHasher verificationCodeHasher;
    private final EmailAuthPolicy emailAuthPolicy;
    private final EmailAuthAttemptService emailAuthAttemptService;

    public LocalDateTime sendCode(String email, EmailAuthPurpose purpose) {
        return sendCode(email, purpose, null, null);
    }

    /**
     * 동기 메일 발송까지 수행하는 편의 메서드.
     * 현재 회원가입/아이디 찾기/비밀번호 찾기/이메일 변경 흐름은
     * 응답 시간 균질화를 위해 issueCode() + AsyncEmailSender 조합을 사용한다.
     */
    public LocalDateTime sendCode(String email, EmailAuthPurpose purpose, String targetUsername, Long targetMemberId) {
        IssuedVerificationCode issued = issueCode(email, purpose, targetUsername, targetMemberId);
        emailSender.sendVerificationCode(email.trim().toLowerCase(), issued.getRawCode(), purpose, issued.getExpiresAt());
        return issued.getExpiresAt();
    }

    public void verifyCode(String email, EmailAuthPurpose purpose, String code) {
        verifyCode(email, purpose, code, null, null);
    }

    public void verifyCode(String email, EmailAuthPurpose purpose, String code, String targetUsername, Long targetMemberId) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedUsername = normalizeUsername(targetUsername);

        validateEmail(normalizedEmail);
        validatePurpose(purpose);
        validateCode(code);

        EmailAuthRequest latestRequest = getLatestRequestOrThrow(
                normalizedEmail,
                purpose,
                normalizedUsername,
                targetMemberId
        );

        if (latestRequest.isExpired()) {
            throw new DomainValidationException("인증코드가 만료되었습니다. 다시 요청해주세요.");
        }

        if (latestRequest.isVerified()) {
            throw new DomainValidationException("이미 인증이 완료되었습니다. 다음 단계를 진행해주세요."); // 이미 인증이 완료된 요청입니다.
        }

        if (latestRequest.isAttemptLimitExceeded()) {
            throw new DomainValidationException("인증 시도 횟수를 초과했습니다. 인증코드를 다시 요청해주세요.");
        }

        boolean matches = verificationCodeHasher.matches(code, latestRequest.getCodeHash());
        if (!matches) {
            boolean limitExceeded = emailAuthAttemptService
                    .increaseAttemptCountAndCheckLimitExceeded(latestRequest.getId());

            if (limitExceeded) {
                throw new DomainValidationException("인증 시도 횟수를 초과했습니다. 인증코드를 다시 요청해주세요.");
            }

            throw new DomainValidationException("인증코드가 올바르지 않습니다. 다시 확인해주세요."); // 인증코드가 올바르지 않습니다.
        }

        latestRequest.verify();
    }

    public boolean isVerified(String email, EmailAuthPurpose purpose) {
        return isVerified(email, purpose, null, null);
    }

    public boolean isVerified(String email, EmailAuthPurpose purpose, String targetUsername, Long targetMemberId) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedUsername = normalizeUsername(targetUsername);

        return findLatestRequest(normalizedEmail, purpose, normalizedUsername, targetMemberId)
                .map(request -> request.isVerified() && !request.isExpired())
                .orElse(false);
    }

    public IssuedPasswordResetSession issuePasswordResetSession(Long memberId, String email) {
        if (memberId == null || memberId < 1L) {
            throw new DomainValidationException("회원 ID는 1 이상이어야 합니다.");
        }

        String normalizedEmail = normalizeEmail(email);
        validateEmail(normalizedEmail);

        String rawResetToken = verificationCodeGenerator.generateResetToken();
        String resetTokenHash = verificationCodeHasher.hash(rawResetToken);
        LocalDateTime expiresAt = emailAuthPolicy.calculatePasswordResetSessionExpiresAt();

        PasswordResetSession session = PasswordResetSession.create(
                memberId,
                normalizedEmail,
                resetTokenHash,
                expiresAt
        );

        PasswordResetSession savedSession = passwordResetSessionRepository.save(session);

        return IssuedPasswordResetSession.of(
                savedSession.getId(),
                rawResetToken,
                expiresAt
        );
    }

    public boolean isValidPasswordResetSession(Long sessionId, String rawToken) {
        PasswordResetSession session = getPasswordResetSessionOrThrow(sessionId);

        if (!session.isAvailable()) {
            return false;
        }

        return verificationCodeHasher.matches(rawToken, session.getResetTokenHash());
    }

    public void consumePasswordResetSession(Long sessionId, String rawToken) {
        PasswordResetSession session = getPasswordResetSessionOrThrow(sessionId);

        if (!session.isAvailable()) {
            throw new DomainValidationException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요."); // 사용할 수 없는 비밀번호 재설정 세션입니다.
        }

        if (!verificationCodeHasher.matches(rawToken, session.getResetTokenHash())) {
            throw new DomainValidationException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요."); // 비밀번호 재설정 세션이 올바르지 않습니다.
        }

        session.markUsed();
    }

    private EmailAuthRequest getLatestRequestOrThrow(
            String email,
            EmailAuthPurpose purpose,
            String targetUsername,
            Long targetMemberId
    ) {
        return findLatestRequest(email, purpose, targetUsername, targetMemberId)
                .orElseThrow(() -> new DomainValidationException("인증코드를 다시 요청해주세요."));
    }

    private Optional<EmailAuthRequest> findLatestRequest(
            String email,
            EmailAuthPurpose purpose,
            String targetUsername,
            Long targetMemberId
    ) {
        if (targetMemberId != null) {
            return emailAuthRequestRepository.findTopByEmailAndPurposeAndTargetMemberIdOrderByCreatedAtDesc(
                    email,
                    purpose,
                    targetMemberId
            );
        }

        if (targetUsername != null && !targetUsername.isBlank()) {
            return emailAuthRequestRepository.findTopByEmailAndPurposeAndTargetUsernameOrderByCreatedAtDesc(
                    email,
                    purpose,
                    targetUsername
            );
        }

        return emailAuthRequestRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
    }

    private PasswordResetSession getPasswordResetSessionOrThrow(Long sessionId) {
        if (sessionId == null || sessionId < 1L) {
            throw new DomainValidationException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요."); // 비밀번호 재설정 세션 ID가 올바르지 않습니다.
        }

        return passwordResetSessionRepository.findById(sessionId)
                .orElseThrow(() -> new DomainValidationException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요.")); // 비밀번호 재설정 세션을 찾을 수 없습니다.
    }

    private void validateResendAllowed(EmailAuthRequest request) {
        LocalDateTime nextAvailableTime = request.getLastRequestedAt()
                .plusSeconds(emailAuthPolicy.getResendIntervalSeconds());

        if (LocalDateTime.now().isBefore(nextAvailableTime)) {
            throw new DomainValidationException("인증코드는 잠시 후 다시 요청해주세요.");
        }
    }

    private void validateRequestCountAllowed(EmailAuthRequest request) {
        if (request.getRequestCount() >= emailAuthPolicy.getMaxRequestCount()) {
            throw new DomainValidationException("인증코드 요청 가능 횟수를 초과했습니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("이메일은 필수입니다.");
        }
    }

    private void validatePurpose(EmailAuthPurpose purpose) {
        if (purpose == null) {
            throw new DomainValidationException("요청을 처리할 수 없습니다. 다시 시도해주세요."); // 이메일 인증 목적은 필수입니다.
        }
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainValidationException("요청을 처리할 수 없습니다. 다시 시도해주세요."); // 인증코드는 필수입니다.
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return username.trim().toLowerCase();
    }

    @Getter
    public static class IssuedPasswordResetSession {
        private final Long sessionId;
        private final String rawToken;
        private final LocalDateTime expiresAt;

        private IssuedPasswordResetSession(Long sessionId, String rawToken, LocalDateTime expiresAt) {
            this.sessionId = sessionId;
            this.rawToken = rawToken;
            this.expiresAt = expiresAt;
        }

        public static IssuedPasswordResetSession of(Long sessionId, String rawToken, LocalDateTime expiresAt) {
            return new IssuedPasswordResetSession(sessionId, rawToken, expiresAt);
        }
    }

    public IssuedVerificationCode issueCode(String email, EmailAuthPurpose purpose) {
        return issueCode(email, purpose, null, null);
    }

    public IssuedVerificationCode issueCode(String email, EmailAuthPurpose purpose, String targetUsername, Long targetMemberId) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedUsername = normalizeUsername(targetUsername);

        validateEmail(normalizedEmail);
        validatePurpose(purpose);

        Optional<EmailAuthRequest> latestRequestOptional = findLatestRequest(
                normalizedEmail,
                purpose,
                normalizedUsername,
                targetMemberId
        );

        String rawCode = verificationCodeGenerator.generateSixDigitCode();
        String codeHash = verificationCodeHasher.hash(rawCode);
        LocalDateTime expiresAt = emailAuthPolicy.calculateCodeExpiresAt();

        if (latestRequestOptional.isPresent()) {
            EmailAuthRequest latestRequest = latestRequestOptional.get();

            validateResendAllowed(latestRequest);
            validateRequestCountAllowed(latestRequest);

            latestRequest.renew(codeHash, expiresAt, latestRequest.getRequestCount() + 1);

            return IssuedVerificationCode.of(rawCode, expiresAt);
        }

        EmailAuthRequest request = EmailAuthRequest.create(
                normalizedEmail,
                purpose,
                codeHash,
                normalizedUsername,
                targetMemberId,
                expiresAt,
                1
        );

        emailAuthRequestRepository.save(request);

        return IssuedVerificationCode.of(rawCode, expiresAt);
    }

    @Getter
    public static class IssuedVerificationCode {
        private final String rawCode;
        private final LocalDateTime expiresAt;

        private IssuedVerificationCode(String rawCode, LocalDateTime expiresAt) {
            this.rawCode = rawCode;
            this.expiresAt = expiresAt;
        }

        public static IssuedVerificationCode of(String rawCode, LocalDateTime expiresAt) {
            return new IssuedVerificationCode(rawCode, expiresAt);
        }
    }
}

