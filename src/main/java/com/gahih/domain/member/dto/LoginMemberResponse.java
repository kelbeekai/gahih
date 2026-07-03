package com.gahih.domain.member.dto;

import com.gahih.domain.member.session.LoginMember;
import lombok.Getter;

@Getter
public class LoginMemberResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String role;
    private final boolean loggedIn;

    public LoginMemberResponse(Long id, String username, String nickname, String email, String role, boolean loggedIn) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.loggedIn = loggedIn;
    }

    public static LoginMemberResponse of(
            Long id,
            String username,
            String nickname,
            String email,
            String role,
            boolean loggedIn
    ) {
        return new LoginMemberResponse(id, username, nickname, email, role, loggedIn);
    }

    public static LoginMemberResponse from(LoginMember loginMember) {
        return of(
                loginMember.getId(),
                loginMember.getUsername(),
                loginMember.getNickname(),
                loginMember.getEmail(),
                loginMember.getRole().name(),
                true
        );
    }

    public static LoginMemberResponse guest() {
        return of(null, null, null, null, null, false);
    }
}
