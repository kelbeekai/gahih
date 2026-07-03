package com.gahih.domain.member.service.recovery;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.entity.Member;
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
public class UsernameRecoveryFacade {

    private static final String SEND_RESPONSE_MESSAGE =
            "입력하신 이메일로 인증 안내 메일을 보냈습니다. 메일함을 확인해주세요.";

    private static final String VERIFY_RESPONSE_MESSAGE =
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

            Member member = memberRepository.findByEmail(normalizedEmail).orElse(null);

            EmailAuthService.IssuedVerificationCode issued =
                    emailAuthService.issueCode(normalizedEmail, EmailAuthPurpose.USERNAME_RECOVERY);

            if (member != null) {
//                emailSender.sendVerificationCode(
                asyncEmailSender.sendVerificationCodeAsync(
                        normalizedEmail,
                        issued.getRawCode(),
                        EmailAuthPurpose.USERNAME_RECOVERY,
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

            emailAuthService.verifyCode(normalizedEmail, EmailAuthPurpose.USERNAME_RECOVERY, code);

            Member member = memberRepository.findByEmail(normalizedEmail).orElse(null);

            if (member != null) {
                String maskedUsername = maskUsername(member.getUsername());
//                emailSender.sendMaskedUsername(normalizedEmail, maskedUsername);
                asyncEmailSender.sendMaskedUsernameAsync(normalizedEmail, maskedUsername);
            }

            return EmailAuthApiResponse.success(VERIFY_RESPONSE_MESSAGE, null, true);
        });
    }

    private String maskUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요."); // 아이디를 확인할 수 없습니다.
        }

        if (username.length() <= 3) {
            return username.charAt(0) + "**";
        }

        String visiblePrefix = username.substring(0, 3);
        return visiblePrefix + "***";
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new DomainValidationException("이메일은 필수입니다.");
        }
        return email.trim().toLowerCase();
    }
}