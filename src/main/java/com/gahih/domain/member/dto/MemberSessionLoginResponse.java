package com.gahih.domain.member.dto;

import com.gahih.domain.member.session.LoginMember;
import lombok.Getter;

@Getter
public class MemberSessionLoginResponse {

    private final String message;
    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String role;

    public MemberSessionLoginResponse(String message, Long id, String username, String nickname, String email, String role) {
        this.message = message;
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

    public static MemberSessionLoginResponse of(String message, Long id, String username, String nickname, String email, String role) {
        return new MemberSessionLoginResponse(message, id, username, nickname, email, role);
    }

    public static MemberSessionLoginResponse from(LoginMember loginMember) {
        return of(
                "로그인에 성공했습니다.",
                loginMember.getId(),
                loginMember.getUsername(),
                loginMember.getNickname(),
                loginMember.getEmail(),
                loginMember.getRole().name()
        );
    }
}
