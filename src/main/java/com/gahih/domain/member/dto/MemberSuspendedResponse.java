package com.gahih.domain.member.dto;

import com.gahih.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberSuspendedResponse {

    private final String username;
    private final String nickname;
    private final LocalDateTime suspendedAt;
    private final LocalDateTime suspendedUntil;
    private final long remainingDays;
    private final boolean permanent;
    private final String reason;

    public MemberSuspendedResponse(
            String username,
            String nickname,
            LocalDateTime suspendedAt,
            LocalDateTime suspendedUntil,
            long remainingDays,
            boolean permanent,
            String reason
    ) {
        this.username = username;
        this.nickname = nickname;
        this.suspendedAt = suspendedAt;
        this.suspendedUntil = suspendedUntil;
        this.remainingDays = remainingDays;
        this.permanent = permanent;
        this.reason = reason;
    }

    public static MemberSuspendedResponse from(Member member) {
        return new MemberSuspendedResponse(
                member.getUsername(),
                member.getNickname(),
                member.getSuspendedAt(),
                member.getSuspendedUntil(),
                member.getSuspensionRemainingDays(),
                member.isPermanentSuspended(),
                member.getSuspensionReason()
        );
    }
}