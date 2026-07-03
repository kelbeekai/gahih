package com.gahih.domain.member.session;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LoginMember {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private final MemberRole role;
    private final MemberStatus status;
    private final LocalDateTime loginAt;
    private final Integer passwordVersionAtLogin;

    public LoginMember(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.role = member.getRole();
        this.status = member.getStatus();
        this.loginAt = LocalDateTime.now();
        this.passwordVersionAtLogin = member.getPasswordVersion() == null ? 0 : member.getPasswordVersion();
    }
}