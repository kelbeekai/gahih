package com.gahih.domain.member.service.auth;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.service.email.AsyncEmailSender;
import com.gahih.domain.member.service.email.EmailAuthService;
import com.gahih.domain.member.service.email.RecoveryResponseDelayService;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpEmailAuthFacade {

    private static final String SEND_RESPONSE_MESSAGE =
            "입력하신 이메일로 인증 안내 메일을 보냈습니다. 메일함을 확인해주세요.";

    private final MemberRepository memberRepository;
    private final EmailAuthService emailAuthService;
//    private final EmailSender emailSender;
    private final AsyncEmailSender asyncEmailSender;
    private final RecoveryResponseDelayService recoveryResponseDelayService;

    @Transactional
    public EmailAuthApiResponse sendCode(String email) {
        return recoveryResponseDelayService.executeWithMinimumDuration(() -> {
            String normalizedEmail = normalizeEmail(email);

            boolean alreadyExists = memberRepository.existsByEmail(normalizedEmail);

            EmailAuthService.IssuedVerificationCode issued =
                    emailAuthService.issueCode(normalizedEmail, EmailAuthPurpose.SIGNUP_VERIFY);

            if (!alreadyExists) {
//                emailSender.sendVerificationCode(
                asyncEmailSender.sendVerificationCodeAsync(
                        normalizedEmail,
                        issued.getRawCode(),
                        EmailAuthPurpose.SIGNUP_VERIFY,
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
    public EmailAuthApiResponse verifyCode(String email, String code) {
        return recoveryResponseDelayService.executeWithMinimumDuration(() -> {
            String normalizedEmail = normalizeEmail(email);

            emailAuthService.verifyCode(normalizedEmail, EmailAuthPurpose.SIGNUP_VERIFY, code);

            return EmailAuthApiResponse.success(
                    "이메일 인증이 완료되었습니다.",
                    null,
                    true
            );
        });
    }

    public boolean isVerified(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return emailAuthService.isVerified(normalizeEmail(email), EmailAuthPurpose.SIGNUP_VERIFY);
    }

    public void validateVerifiedOrThrow(String email) {
        if (!isVerified(email)) {
            throw new BusinessException("이메일 인증을 완료해주세요.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new DomainValidationException("이메일은 필수입니다.");
        }
        return email.trim().toLowerCase();
    }
}