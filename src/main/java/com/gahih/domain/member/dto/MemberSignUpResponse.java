package com.gahih.domain.member.dto;

import com.gahih.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberSignUpResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final String role;
    private final String status;

    public MemberSignUpResponse(Long id, String username, String nickname, String email, String role, String status) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public static MemberSignUpResponse of(Long id, String username, String nickname, String email, String role, String status) {
        return new MemberSignUpResponse(id, username, nickname, email, role, status);
    }

    public static MemberSignUpResponse from(Member member) {
        return of(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getEmail(),
                member.getRole().name(),
                member.getStatus().name()
        );
    }
}
