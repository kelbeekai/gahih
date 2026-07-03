package com.gahih.domain.member.dto;

import com.gahih.domain.member.policy.MemberValidationPolicy;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberSignUpRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(
            min = MemberValidationPolicy.USERNAME_MIN_LENGTH,
            max = MemberValidationPolicy.USERNAME_MAX_LENGTH,
            message = "아이디는 4자 이상 20자 이하여야 합니다."
    )
    @Pattern(
            regexp = MemberValidationPolicy.USERNAME_REGEX,
            message = "아이디는 영문 소문자, 숫자, 밑줄(_), 마침표(.)만 사용할 수 있으며 시작과 끝은 영문 소문자 또는 숫자여야 합니다."
    )
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(
            min = MemberValidationPolicy.RAW_PASSWORD_MIN_LENGTH,
            max = MemberValidationPolicy.RAW_PASSWORD_MAX_LENGTH,
            message = "비밀번호는 8자 이상 64자 이하여야 합니다."
    )
    @Pattern(
            regexp = MemberValidationPolicy.PASSWORD_POLICY_REGEX,
            message = "비밀번호는 공백 없이 숫자 1개 이상과 특수문자 1개 이상을 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(
            min = MemberValidationPolicy.NICKNAME_MIN_LENGTH,
            max = MemberValidationPolicy.NICKNAME_MAX_LENGTH,
            message = "닉네임은 2자 이상 12자 이하여야 합니다."
    )
    @Pattern(
            regexp = MemberValidationPolicy.NICKNAME_REGEX,
            message = "닉네임은 한글, 영문, 숫자, 밑줄(_)만 사용할 수 있습니다."
    )
    private String nickname;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(
            min = MemberValidationPolicy.EMAIL_MIN_LENGTH,
            max = MemberValidationPolicy.EMAIL_MAX_LENGTH,
            message = "이메일은 5자 이상 100자 이하여야 합니다."
    )
    private String email;

    @AssertTrue(message = "이용약관에 동의해주세요.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보 처리방침에 동의해주세요.")
    private boolean privacyAgreed;

    @AssertTrue(message = "커뮤니티 운영정책에 동의해주세요.")
    private boolean policyAgreed;
}