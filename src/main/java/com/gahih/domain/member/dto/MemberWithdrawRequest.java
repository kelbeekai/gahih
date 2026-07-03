package com.gahih.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberWithdrawRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}