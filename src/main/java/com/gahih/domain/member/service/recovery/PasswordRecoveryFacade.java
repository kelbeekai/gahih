package com.gahih.domain.member.service;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.dto.MemberPasswordResetRequest;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.service.email.AsyncEmailSender;
import com.gahih.domain.member.service.email.EmailAuthService;
import com.gahih.domain.member.service.email.EmailSender;
import com.gahih.domain.member.service.email.RecoveryResponseDelayService;
import com.gahih.global.common.SessionConst;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordRecoveryFacade {

    private static final String SEND_RESPONSE_MESSAGE =
            "입력한 정보가 유효할 경우 안내 메일을 받을 수 있습니다. 메일함을 확인해주세요."; // 입력한 정보가 유효하면 안내 메일을 보냈습니다. 메일함을 확인해주세요.

    private static final String VERIFY_RESPONSE_MESSAGE =
            "인증이 완료되었습니다. 새 비밀번호를 설정해주세요.";

    private final MemberRepository memberRepository;
    private final EmailAuthService emailAuthService;
//    private final EmailSender emailSender;
    private final AsyncEmailSender asyncEmailSender;
    private final RecoveryResponseDelayService recoveryResponseDelayService;
    private final MemberAccountService memberAccountService;

    @Transactional
    public EmailAuthApiResponse sendCode(String username, String email) {
        return recoveryResponseDelayService.executeWithMinimumDuration(() -> {
            String normalizedUsername = normalizeUsername(username);
            String normalizedEmail = normalizeEmail(email);

            Member member = memberRepository.findByUsernameAndEmail(normalizedUsername, normalizedEmail)
                    .orElse(null);

            EmailAuthService.IssuedVerificationCode issued =
                    emailAuthService.issueCode(
                            normalizedEmail,
                            EmailAuthPurpose.PASSWORD_RESET_VERIFY,
                            normalizedUsername,
                            null
                    );

            if (member != null && !member.isDeleted()) {
//                emailSender.sendVerificationCode(
                asyncEmailSender.sendVerificationCodeAsync(
                        normalizedEmail,
                        issued.getRawCode(),
                        EmailAuthPurpose.PASSWORD_RESET_VERIFY,
                        issued.getExpiresAt()
                );
            }

            return EmailAuthApiResponse.success(
                    SEND_RESPONSE_MESSAGE,
                    issued.getExpiresAt(),
                    false
            );
        });
    }

    @Transactional
    public EmailAuthApiResponse verifyCode(String username, String email, String code, HttpSession session) {
        return recoveryResponseDelayService.executeWithMinimumDuration(() -> {
            clearPasswordResetSession(session);

            String normalizedUsername = normalizeUsername(username);
            String normalizedEmail = normalizeEmail(email);

            emailAuthService.verifyCode(
                    normalizedEmail,
                    EmailAuthPurpose.PASSWORD_RESET_VERIFY,
                    code,
                    normalizedUsername,
                    null
            );

            Member member = memberRepository.findByUsernameAndEmail(normalizedUsername, normalizedEmail)
                    .orElse(null);

            if (member != null && !member.isDeleted()) {
                EmailAuthService.IssuedPasswordResetSession issuedSession =
                        emailAuthService.issuePasswordResetSession(member.getId(), normalizedEmail);

                session.setAttribute(SessionConst.PASSWORD_RESET_SESSION_ID, issuedSession.getSessionId());
                session.setAttribute(SessionConst.PASSWORD_RESET_TOKEN, issuedSession.getRawToken());
                session.setAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID, member.getId());

//                emailSender.sendPasswordResetReadyNotice(normalizedEmail);
                asyncEmailSender.sendPasswordResetReadyNoticeAsync(normalizedEmail);
            }

            return EmailAuthApiResponse.success(VERIFY_RESPONSE_MESSAGE, null, true);
        });
    }

    public boolean hasValidPasswordResetSession(HttpSession session) {
        if (session == null) {
            return false;
        }

        Long sessionId = (Long) session.getAttribute(SessionConst.PASSWORD_RESET_SESSION_ID);
        String rawToken = (String) session.getAttribute(SessionConst.PASSWORD_RESET_TOKEN);
        Long memberId = (Long) session.getAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID);

        if (sessionId == null || rawToken == null || memberId == null) {
            return false;
        }

        return emailAuthService.isValidPasswordResetSession(sessionId, rawToken);
    }

    @Transactional
    public void resetPassword(HttpSession session, MemberPasswordResetRequest request) {
        if (session == null) {
            throw new BusinessException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요."); // 비밀번호 재설정 세션이 없습니다.
        }

        Long sessionId = (Long) session.getAttribute(SessionConst.PASSWORD_RESET_SESSION_ID);
        String rawToken = (String) session.getAttribute(SessionConst.PASSWORD_RESET_TOKEN);
        Long memberId = (Long) session.getAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID);

        if (sessionId == null || rawToken == null || memberId == null) {
            throw new BusinessException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요."); // 비밀번호 재설정 인증이 필요합니다.
        }

        if (!emailAuthService.isValidPasswordResetSession(sessionId, rawToken)) {
            clearPasswordResetSession(session);
            throw new BusinessException("비밀번호 재설정 인증이 만료되었거나 유효하지 않습니다. 다시 시도해주세요."); // 비밀번호 재설정 세션이 유효하지 않습니다. 다시 시도해주세요.
        }

        memberAccountService.resetPasswordByRecovery(memberId, request);
        emailAuthService.consumePasswordResetSession(sessionId, rawToken);
        clearPasswordResetSession(session);
    }

    public void clearPasswordResetSession(HttpSession session) {
        if (session == null) {
            return;
        }

        session.removeAttribute(SessionConst.PASSWORD_RESET_SESSION_ID);
        session.removeAttribute(SessionConst.PASSWORD_RESET_TOKEN);
        session.removeAttribute(SessionConst.PASSWORD_RESET_MEMBER_ID);
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new DomainValidationException("아이디는 필수입니다.");
        }
        return username.trim().toLowerCase();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainValidationException("이메일은 필수입니다.");
        }
        return email.trim().toLowerCase();
    }
}