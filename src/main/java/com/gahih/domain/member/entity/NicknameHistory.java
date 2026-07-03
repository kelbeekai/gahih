package com.gahih.domain.member.entity;

import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nickname_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameHistory {

    private static final int NICKNAME_MAX_LENGTH = 50;

    private static final String ANONYMIZED_NICKNAME_TEXT = "비식별 처리됨";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(length = NICKNAME_MAX_LENGTH)
    private String previousNickname;

    @Column(length = NICKNAME_MAX_LENGTH)
    private String newNickname;

    @Column(nullable = false, length = 30)
    private String changeType;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private NicknameHistory(
            Member member,
            String previousNickname,
            String newNickname,
            String changeType,
            LocalDateTime changedAt
    ) {
        validateMember(member);
        validateNickname(previousNickname, "이전 닉네임", true);
        validateNickname(newNickname, "변경 후 닉네임", true);
        validateChangeType(changeType);
        validateChangedAt(changedAt);

        if (previousNickname == null && newNickname == null) {
            throw new DomainValidationException("이전 닉네임과 변경 후 닉네임은 둘 다 null일 수 없습니다.");
        }

        this.member = member;
        this.previousNickname = previousNickname;
        this.newNickname = newNickname;
        this.changeType = changeType;
        this.changedAt = changedAt;
    }

    /**
     * 회원가입 시 최초 닉네임 기록
     */
    public static NicknameHistory createInitial(Member member, String initialNickname) {
        return new NicknameHistory(member, null, initialNickname, "INITIAL", LocalDateTime.now());
    }

    /**
     * 닉네임 변경 시 기록
     */
    public static NicknameHistory createUserChange(Member member, String previousNickname, String newNickname) {
        return new NicknameHistory(member, previousNickname, newNickname, "USER_CHANGED", LocalDateTime.now());
    }

    /**
     * 관리자 강제 변경 시 기록
     */
    public static NicknameHistory createAdminForced(Member member, String previousNickname, String newNickname) {
        return new NicknameHistory(member, previousNickname, newNickname, "ADMIN_FORCED", LocalDateTime.now());
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new DomainValidationException("닉네임 변경 이력의 회원은 필수입니다.");
        }
    }

    private void validateNickname(String nickname, String fieldName, boolean nullableAllowed) {
        if (nickname == null) {
            if (nullableAllowed) {
                return;
            }
            throw new DomainValidationException(fieldName + "은(는) null일 수 없습니다.");
        }

        if (nickname.isBlank()) {
            throw new DomainValidationException(fieldName + "은(는) 비어 있을 수 없습니다.");
        }

        if (nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new DomainValidationException(fieldName + "은(는) 50자를 초과할 수 없습니다.");
        }
    }

    private void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new DomainValidationException("닉네임 변경 이력의 변경 시각은 필수입니다.");
        }
    }

    private void validateChangeType(String changeType) {
        if (changeType == null || changeType.isBlank()) {
            throw new DomainValidationException("닉네임 변경 이력 구분값은 필수입니다.");
        }
        if (changeType.length() > 30) {
            throw new DomainValidationException("닉네임 변경 이력 구분값은 30자를 초과할 수 없습니다.");
        }
    }

    public void anonymizeNicknameFields() {
        if (this.previousNickname != null && !this.previousNickname.isBlank()) {
            this.previousNickname = ANONYMIZED_NICKNAME_TEXT;
        }

        if (this.newNickname != null && !this.newNickname.isBlank()) {
            this.newNickname = ANONYMIZED_NICKNAME_TEXT;
        }
    }
}