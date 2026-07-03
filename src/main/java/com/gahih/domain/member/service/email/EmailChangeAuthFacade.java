package com.gahih.domain.member.service;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.EmailAuthPurpose;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.service.email.AsyncEmailSender;
import com.gahih.domain.member.service.email.EmailAuthService;
import com.gahih.domain.member.service.email.EmailSender;
import com.gahih.domain.member.service.email.RecoveryResponseDelayService;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailChangeAuthFacade {

    private static final String SEND_RESPONSE_MESSAGE =
            "입력한 이메일로 인증 안내를 보냈습니다. 메일함을 확인해주세요.";

    private final MemberRepository memberRepository;
    private final EmailAuthService emailAuthService;
//    private final EmailSender emailSender;
    private final AsyncEmailSender asyncEmailSender;
    private final RecoveryResponseDelayService recoveryResponseDelayService;

    @Transactional
    public EmailAuthApiResponse sendCode(Long memberId, String email) {
        return recoveryResponseDelayService.executeWithMinimumDuration(() -> {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

            String normalizedEmail = normalizeEmail(email);

            Member existingMember = memberRepository.findByEmail(normalizedEmail).orElse(null);

            EmailAuthService.IssuedVerificationCode issued =
                    emailAuthService.issueCode(
                            normalizedEmail,
                            EmailAuthPurpose.EMAIL_CHANGE_VERIFY,
                            null,
                            memberId
                    );

            boolean sameAsCurrent = member.getEmail().equals(normalizedEmail);
            boolean usedByAnother = existingMember != null && !existingMember.getId().equals(memberId);

            if (!sameAsCurrent && !usedByAnother) {
//                emailSender.sendVerificationCode(
                asyncEmailSender.sendVerificationCodeAsync(
                        normalizedEmail,
                        issued.getRawCode(),
                        EmailAuthPurpose.EMAIL_CHANGE_VERIFY,
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
    public EmailAuthApiResponse verifyCode(Long memberId, String email, String code) {
        return recoveryResponseDelayService.executeWithMinimumDuration(() -> {
            String normalizedEmail = normalizeEmail(email);

            emailAuthService.verifyCode(
                    normalizedEmail,
                    EmailAuthPurpose.EMAIL_CHANGE_VERIFY,
                    code,
                    null,
                    memberId
            );

            return EmailAuthApiResponse.success(
                    "이메일 인증이 완료되었습니다.",
                    null,
                    true
            );
        });
    }

    public boolean isVerified(String email, Long memberId) {
        if (email == null || email.isBlank() || memberId == null) {
            return false;
        }

        return emailAuthService.isVerified(
                normalizeEmail(email),
                EmailAuthPurpose.EMAIL_CHANGE_VERIFY,
                null,
                memberId
        );
    }

    public void validateVerifiedOrThrow(String email, Long memberId) {
        if (!isVerified(email, memberId)) {
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