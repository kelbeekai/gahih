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
public class MemberPasswordChangeRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(
            min = MemberValidationPolicy.RAW_PASSWORD_MIN_LENGTH,
            max = MemberValidationPolicy.RAW_PASSWORD_MAX_LENGTH,
            message = "새 비밀번호는 8자 이상 64자 이하여야 합니다."
    )
    @Pattern(
            regexp = MemberValidationPolicy.PASSWORD_POLICY_REGEX,
            message = "새 비밀번호는 공백 없이 숫자 1개 이상과 특수문자 1개 이상을 포함해야 합니다."
    )
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;
}