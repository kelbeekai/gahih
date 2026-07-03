package com.gahih.domain.member.enumtype;

public enum EmailAuthPurpose {
    SIGNUP_VERIFY,          // 회원가입 이메일 인증
    USERNAME_RECOVERY,      // 아이디 찾기 이메일 인증
    PASSWORD_RESET_VERIFY,  // 비밀번호 재설정 전 이메일 인증
    EMAIL_CHANGE_VERIFY     // 회원정보 수정 시 이메일 변경 인증
}