package com.gahih.domain.member.dto;

import com.gahih.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberWithdrawnResponse {

    private final String username;
    private final String nickname;
    private final LocalDateTime withdrawnAt;
    private final LocalDateTime withdrawExpireAt;
    private final long remainingDays;
    private final boolean expired;

    public MemberWithdrawnResponse(
            String username,
            String nickname,
            LocalDateTime withdrawnAt,
            LocalDateTime withdrawExpireAt,
            long remainingDays,
            boolean expired
    ) {
        this.username = username;
        this.nickname = nickname;
        this.withdrawnAt = withdrawnAt;
        this.withdrawExpireAt = withdrawExpireAt;
        this.remainingDays = remainingDays;
        this.expired = expired;
    }

    public static MemberWithdrawnResponse from(Member member) {
        return new MemberWithdrawnResponse(
                member.getUsername(),
                member.getNickname(),
                member.getWithdrawnAt(),
                member.getWithdrawExpireAt(),
                member.getWithdrawRemainingDays(),
                member.isWithdrawExpired()
        );
    }
}