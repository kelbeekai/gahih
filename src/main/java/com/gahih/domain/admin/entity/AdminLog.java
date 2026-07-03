package com.gahih.domain.admin.entity;

import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLog {

    private static final String ANONYMIZED_TEXT = "비식별 처리됨";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member admin;

    @Column(nullable = false, length = 100)
    private String action;

    private Long targetId;

    @Column(length = 255)
    private String targetName;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminLogTargetType targetType;

    @Lob
    private String reason;

    @Lob
    private String beforeSnapshot;

    @Lob
    private String afterSnapshot;

    @Column(length = 20)
    private String targetCommunityCode;

    @Column(length = 100)
    private String targetCommunityName;

    private AdminLog(
            Member admin,
            String action,
            AdminLogTargetType targetType,
            Long targetId,
            String targetName,
            String reason,
            String beforeSnapshot,
            String afterSnapshot,
            String targetCommunityCode,
            String targetCommunityName
    ) {
        this.admin = admin;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.reason = reason;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        this.targetCommunityCode = targetCommunityCode;
        this.targetCommunityName = targetCommunityName;
    }

    public static AdminLog create(
            Member admin,
            String action,
            AdminLogTargetType targetType,
            Long targetId,
            String targetName,
            String reason,
            String beforeSnapshot,
            String afterSnapshot,
            String targetCommunityCode,
            String targetCommunityName
    ) {
        return new AdminLog(
                admin,
                action,
                targetType,
                targetId,
                targetName,
                reason,
                beforeSnapshot,
                afterSnapshot,
                targetCommunityCode,
                targetCommunityName
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void anonymizeMemberNicknameData(String originalNickname) {
        if (this.targetName != null && !this.targetName.isBlank()) {
            this.targetName = ANONYMIZED_TEXT;
        }

        if (this.reason != null && !this.reason.isBlank() && originalNickname != null && !originalNickname.isBlank()) {
            this.reason = this.reason.replace(originalNickname, ANONYMIZED_TEXT);
        }

        if (this.beforeSnapshot != null && !this.beforeSnapshot.isBlank()) {
            this.beforeSnapshot = anonymizeSnapshot(this.beforeSnapshot, originalNickname);
        }

        if (this.afterSnapshot != null && !this.afterSnapshot.isBlank()) {
            this.afterSnapshot = anonymizeSnapshot(this.afterSnapshot, originalNickname);
        }
    }

    private String anonymizeSnapshot(String snapshot, String originalNickname) {
        String anonymized = snapshot
                .replaceAll("nickname=[^,]+", "nickname=" + ANONYMIZED_TEXT)
                .replaceAll("username=[^,]+", "username=" + ANONYMIZED_TEXT)
                .replaceAll("email=[^,]+", "email=" + ANONYMIZED_TEXT)
                .replaceAll("targetNameSnapshot=[^,]+", "targetNameSnapshot=" + ANONYMIZED_TEXT)
                .replaceAll("writerNicknameSnapshot=[^,]+", "writerNicknameSnapshot=" + ANONYMIZED_TEXT);

        if (originalNickname != null && !originalNickname.isBlank()) {
            anonymized = anonymized.replace(originalNickname, ANONYMIZED_TEXT);
        }

        return anonymized;
    }
}