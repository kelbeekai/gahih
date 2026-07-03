package com.gahih.domain.member.policy;

import java.util.List;

public final class MemberValidationPolicy {

    private MemberValidationPolicy() {
    }

    public static final int USERNAME_MIN_LENGTH = 4;
    public static final int USERNAME_MAX_LENGTH = 20;

    public static final int RAW_PASSWORD_MIN_LENGTH = 8;
    public static final int RAW_PASSWORD_MAX_LENGTH = 64;
    public static final int ENCODED_PASSWORD_MAX_LENGTH = 255;

    public static final int NICKNAME_MIN_LENGTH = 2;
    public static final int NICKNAME_MAX_LENGTH = 12;

    public static final int EMAIL_MIN_LENGTH = 5;
    public static final int EMAIL_MAX_LENGTH = 100;

    public static final String USERNAME_REGEX =
            "^[a-z0-9](?:[a-z0-9._]{2,18})[a-z0-9]$";

    public static final String NICKNAME_REGEX =
            "^[가-힣A-Za-z0-9_]{2,12}$";

    public static final String PASSWORD_POLICY_REGEX =
            "^(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/\\\\|`~])\\S{8,64}$";

    public static final List<String> FORBIDDEN_NICKNAME_PREFIXES = List.of(
            "user_",
            "username_",
            "deleted_",
            "system_",
            "탈퇴",
            "비식별"
    );

    public static final List<String> FORBIDDEN_NICKNAME_EXACT = List.of(
            "[비식별 처리됨]",
            "비식별 처리됨"
    );

    public static final int PASSWORD_MIN_LENGTH = RAW_PASSWORD_MIN_LENGTH;
    public static final int PASSWORD_MAX_LENGTH = RAW_PASSWORD_MAX_LENGTH;
    public static final String PASSWORD_PATTERN = PASSWORD_POLICY_REGEX;
    public static final String PASSWORD_MESSAGE =
            "비밀번호는 8자 이상 64자 이하이며, 숫자와 특수문자를 포함하고 공백은 사용할 수 없습니다.";
}