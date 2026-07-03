package com.gahih.domain.member.service.email;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class VerificationCodeGenerator {

    private static final int VERIFICATION_CODE_MIN = 100000;
    private static final int VERIFICATION_CODE_BOUND = 900000;
    private static final int RESET_TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSixDigitCode() {
        int value = secureRandom.nextInt(VERIFICATION_CODE_BOUND) + VERIFICATION_CODE_MIN;
        return String.valueOf(value);
    }

    public String generateResetToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}