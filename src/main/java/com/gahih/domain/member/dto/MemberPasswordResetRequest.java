package com.gahih.domain.member.dto;

import com.gahih.domain.member.policy.MemberValidationPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberPasswordResetRequest {

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(
            min = MemberValidationPolicy.PASSWORD_MIN_LENGTH,
            max = MemberValidationPolicy.PASSWORD_MAX_LENGTH,
            message = "비밀번호는 8자 이상 20자 이하여야 합니다."
    )
    @Pattern(
            regexp = MemberValidationPolicy.PASSWORD_PATTERN,
            message = MemberValidationPolicy.PASSWORD_MESSAGE
    )
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;
}