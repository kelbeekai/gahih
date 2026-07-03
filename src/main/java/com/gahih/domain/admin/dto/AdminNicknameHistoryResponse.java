package com.gahih.domain.admin.dto;

import com.gahih.domain.member.enumtype.MemberStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminNicknameHistoryResponse {

    private final Long id;
    private final Long memberId;
    private final String currentNickname;
    private final MemberStatus currentStatus;
    private final String previousNickname;
    private final String newNickname;
    private final String changeType;
    private final LocalDateTime changedAt;

    public AdminNicknameHistoryResponse(
            Long id,
            Long memberId,
            String currentNickname,
            MemberStatus currentStatus,
            String previousNickname,
            String newNickname,
            String changeType,
            LocalDateTime changedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.currentNickname = currentNickname;
        this.currentStatus = currentStatus;
        this.previousNickname = previousNickname;
        this.newNickname = newNickname;
        this.changeType = changeType;
        this.changedAt = changedAt;
    }

    public String getDisplayCurrentNickname() {
        if (currentStatus == com.gahih.domain.member.enumtype.MemberStatus.DELETED) {
            return "최종 종료된 회원";
        }
        if (currentStatus == com.gahih.domain.member.enumtype.MemberStatus.WITHDRAWN) {
            return "탈퇴 유예 회원";
        }
        return currentNickname == null || currentNickname.isBlank() ? "-" : currentNickname;
    }

    public String getDisplayPreviousNickname() {
        return previousNickname == null || previousNickname.isBlank() ? "-" : previousNickname;
    }

    public String getDisplayNewNickname() {
        return newNickname == null || newNickname.isBlank() ? "-" : newNickname;
    }

    public String getDisplayChangeType() {
        return switch (changeType) {
            case "INITIAL" -> "최초 설정";
            case "USER_CHANGED" -> "닉네임 변경";
            case "ADMIN_FORCED" -> "관리자 조치";
            default -> "이력";
        };
    }
}