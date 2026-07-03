package com.gahih.domain.member.dto;

import com.gahih.domain.member.policy.MemberValidationPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordRecoveryVerifyCodeRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(
            min = MemberValidationPolicy.USERNAME_MIN_LENGTH,
            max = MemberValidationPolicy.USERNAME_MAX_LENGTH,
            message = "아이디는 4자 이상 20자 이하여야 합니다."
    )
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(
            min = MemberValidationPolicy.EMAIL_MIN_LENGTH,
            max = MemberValidationPolicy.EMAIL_MAX_LENGTH,
            message = "이메일은 5자 이상 100자 이하여야 합니다."
    )
    private String email;

    @NotBlank(message = "인증코드는 필수입니다.")
    @Pattern(regexp = "\\d{6}", message = "인증코드는 6자리 숫자여야 합니다.")
    private String code;
}