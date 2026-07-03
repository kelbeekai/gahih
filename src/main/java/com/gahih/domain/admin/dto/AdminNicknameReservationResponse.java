package com.gahih.domain.admin.dto;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class AdminNicknameReservationResponse {

    private final Long id;
    private final String nickname;
    private final String reasonType;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    public AdminNicknameReservationResponse(
            Long id,
            String nickname,
            String reasonType,
            LocalDateTime expiresAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.nickname = nickname;
        this.reasonType = reasonType;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getDisplayReasonType() {
        return switch (reasonType) {
            case "NICKNAME_CHANGED" -> "닉네임 변경";
            case "ADMIN_FORCED" -> "관리자 조치";
            case "MEMBER_FINALIZED" -> "최종 종료";
            default -> reasonType;
        };
    }

    public String getRemainingTimeText() {
        LocalDateTime now = LocalDateTime.now();

        if (!expiresAt.isAfter(now)) {
            return "만료됨";
        }

        Duration duration = Duration.between(now, expiresAt);
        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (60 * 24);
        long hours = (totalMinutes % (60 * 24)) / 60;
        long minutes = totalMinutes % 60;

        if (days > 0) {
            return days + "일 " + hours + "시간";
        }
        if (hours > 0) {
            return hours + "시간 " + minutes + "분";
        }
        return Math.max(minutes, 0) + "분";
    }
}