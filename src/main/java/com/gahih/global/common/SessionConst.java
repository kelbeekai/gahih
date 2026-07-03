package com.gahih.global.common;

public final class SessionConst {

    private SessionConst() {
    }

    public static final String LOGIN_MEMBER = "loginMember";
    public static final String COUNT_HISTORY = "countHistory";

    public static final String PASSWORD_RESET_SESSION_ID = "passwordResetSessionId";
    public static final String PASSWORD_RESET_TOKEN = "passwordResetToken";
    public static final String PASSWORD_RESET_MEMBER_ID = "passwordResetMemberId";

    /**
     * 작성 직후 보호가 활성화된 postId 집합
     */
    public static final String ACTIVE_CREATED_POST_BYPASS = "activeCreatedPostBypass";
}