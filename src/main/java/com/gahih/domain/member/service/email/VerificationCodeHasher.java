package com.gahih.domain.member.service.email;

import com.gahih.global.exception.DomainValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeHasher {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String hash(String rawValue) {
        validateRawValue(rawValue);
        return passwordEncoder.encode(rawValue);
    }

    public boolean matches(String rawValue, String encodedValue) {
        validateRawValue(rawValue);

        if (encodedValue == null || encodedValue.isBlank()) {
            throw new DomainValidationException("인코딩된 값은 필수입니다.");
        }

        return passwordEncoder.matches(rawValue, encodedValue);
    }

    private void validateRawValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new DomainValidationException("원본 값은 필수입니다.");
        }
    }
}