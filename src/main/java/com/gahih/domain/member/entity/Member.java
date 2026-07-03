package com.gahih.domain.member.entity;

import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.policy.MemberValidationPolicy;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final int USERNAME_MAX_LENGTH = MemberValidationPolicy.USERNAME_MAX_LENGTH;
    private static final int PASSWORD_MAX_LENGTH = MemberValidationPolicy.ENCODED_PASSWORD_MAX_LENGTH;
    private static final int NICKNAME_MAX_LENGTH = MemberValidationPolicy.NICKNAME_MAX_LENGTH;
    private static final int EMAIL_MAX_LENGTH = MemberValidationPolicy.EMAIL_MAX_LENGTH;

    private static final Pattern USERNAME_PATTERN = Pattern.compile(MemberValidationPolicy.USERNAME_REGEX);
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(MemberValidationPolicy.NICKNAME_REGEX);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = USERNAME_MAX_LENGTH)
    private String username;

    @Column(nullable = false, length = PASSWORD_MAX_LENGTH)
    private String password;

    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    private Integer passwordVersion = 0;

    @Column(nullable = false, unique = true, length = NICKNAME_MAX_LENGTH)
    private String nickname;

    @Column(nullable = false, unique = true, length = EMAIL_MAX_LENGTH)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    private LocalDateTime withdrawnAt;
    private LocalDateTime withdrawExpireAt;
    private LocalDateTime nicknameChangedAt;
    private LocalDateTime finalizedAt;

    private LocalDateTime suspendedAt;
    private LocalDateTime suspendedUntil;

    @Lob
    private String suspensionReason;

    @Column(nullable = false)
    private Integer suspensionCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private Member(String username, String password, String nickname, String email,
                   MemberRole role, MemberStatus status) {
        validateUsername(username);
        validatePassword(password);
        validateNickname(nickname);
        validateEmail(email);
        validateRole(role);
        validateStatus(status);

        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public static Member createUser(String username, String encodedPassword, String nickname, String email) {
        return new Member(
                username,
                encodedPassword,
                nickname,
                email,
                MemberRole.USER,
                MemberStatus.ACTIVE
        );
    }

    public static Member createAdmin(String username, String encodedPassword, String nickname, String email) {
        return new Member(
                username,
                encodedPassword,
                nickname,
                email,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.passwordChangedAt == null) {
            this.passwordChangedAt = this.createdAt;
        }

        if (this.passwordVersion == null) {
            this.passwordVersion = 0;
        }

        if (this.status == null) {
            this.status = MemberStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeNickname(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
        this.nicknameChangedAt = LocalDateTime.now();
    }

    public void changeEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    public void changePassword(String encodedPassword) {
        validatePassword(encodedPassword);
        this.password = encodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.passwordVersion = (this.passwordVersion == null ? 0 : this.passwordVersion) + 1;
    }

    public void promoteToAdmin() {
        if (isWithdrawn() || isDeleted()) {
            throw new DomainValidationException("탈퇴 또는 삭제된 회원은 관리자로 변경할 수 없습니다.");
        }
        this.role = MemberRole.ADMIN;
    }

    public void demoteToUser() {
        if (isWithdrawn() || isDeleted()) {
            throw new DomainValidationException("탈퇴 또는 삭제된 회원은 일반회원으로 변경할 수 없습니다.");
        }
        this.role = MemberRole.USER;
    }

    public void suspend() {
        suspendPermanently(null);
    }

    public void suspendTemporarily(LocalDateTime suspendedUntil, String reason) {
        if (isDeleted()) {
            throw new DomainValidationException("삭제 처리된 회원은 정지 상태로 변경할 수 없습니다.");
        }
        if (suspendedUntil == null) {
            throw new DomainValidationException("기간정지 만료 시각은 필수입니다.");
        }
        if (!suspendedUntil.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("기간정지 만료 시각은 현재 시각 이후여야 합니다.");
        }

        this.status = MemberStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspendedUntil = suspendedUntil;
        this.suspensionReason = normalizeLongText(reason);
        this.suspensionCount = (this.suspensionCount == null ? 0 : this.suspensionCount) + 1;
    }

    public void suspendPermanently(String reason) {
        if (isDeleted()) {
            throw new DomainValidationException("삭제 처리된 회원은 정지 상태로 변경할 수 없습니다.");
        }

        this.status = MemberStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspendedUntil = null;
        this.suspensionReason = normalizeLongText(reason);
    }

    public void releaseSuspension() {
        if (!isSuspended()) {
            throw new DomainValidationException("정지 상태의 회원만 정지를 해제할 수 있습니다.");
        }

        this.status = MemberStatus.ACTIVE;
        this.suspendedAt = null;
        this.suspendedUntil = null;
        this.suspensionReason = null;
    }

    public boolean isTemporarySuspended() {
        return isSuspended() && suspendedUntil != null;
    }

    public boolean isPermanentSuspended() {
        return isSuspended() && suspendedUntil == null;
    }

    public boolean isSuspensionExpired() {
        return isTemporarySuspended()
                && LocalDateTime.now().isAfter(suspendedUntil);
    }

    public long getSuspensionRemainingDays() {
        if (!isTemporarySuspended()) {
            return 0L;
        }

        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), suspendedUntil);
        return Math.max(days, 0L);
    }

    private String normalizeLongText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public void activate() {
        if (isDeleted()) {
            throw new DomainValidationException("삭제 처리된 회원은 다시 활성화할 수 없습니다.");
        }
        this.status = MemberStatus.ACTIVE;
        this.withdrawnAt = null;
        this.withdrawExpireAt = null;
        this.suspendedAt = null;
        this.suspendedUntil = null;
        this.suspensionReason = null;
    }

    public void withdraw() {
        if (isDeleted()) {
            throw new DomainValidationException("삭제 처리된 회원은 탈퇴 처리할 수 없습니다.");
        }
        if (isWithdrawn()) {
            throw new DomainValidationException("이미 탈퇴한 회원입니다.");
        }
        if (isSuspended()) {
            throw new DomainValidationException("이용 정지된 회원은 탈퇴할 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = now;
        this.withdrawExpireAt = calculateWithdrawExpireAt(now);
    }

    private LocalDateTime calculateWithdrawExpireAt(LocalDateTime now) {
        // 운영 정책: 30일 유예
        return now.plusDays(30);

        // 빠른 테스트용 예시
//         return now.plusMinutes(2);
        // return now.plusMinutes(5);

        /* DB 에서 만료 시각을 과거로 바꿔서 강제 트리거 예시 (H2)

         */
    }

    /**
     * legacy 상태 전이, 현재 정책상 미사용
     */
    public void markDeleted() {
        if (isWithdrawn()) {
            throw new DomainValidationException("탈퇴한 회원은 관리자 삭제 상태로 변경할 수 없습니다.");
        }
        if (isDeleted()) {
            throw new DomainValidationException("이미 삭제 처리된 회원입니다.");
        }

        this.status = MemberStatus.DELETED;
        this.withdrawnAt = null;
        this.withdrawExpireAt = null;
    }

    /**
     * 개인정보 비식별화까지 포함한 운영 최종 종료
     */
    public void finalizeDeletion(String anonymizedUsername,
                                 String anonymizedNickname,
                                 String anonymizedEmail,
                                 String encodedPassword) {
        if (isDeleted()) {
            throw new DomainValidationException("이미 최종 종료 처리된 회원입니다.");
        }

        this.username = anonymizedUsername;
        this.nickname = anonymizedNickname;
        this.email = anonymizedEmail;
        this.password = encodedPassword;
        this.status = MemberStatus.DELETED;
        this.withdrawnAt = null;
        this.withdrawExpireAt = null;
        this.suspendedAt = null;
        this.suspendedUntil = null;
        this.suspensionReason = null;
        this.finalizedAt = LocalDateTime.now();
    }

    public void restoreFromWithdraw() {
        if (!isWithdrawn()) {
            throw new DomainValidationException("탈퇴 유예 상태의 회원만 복구할 수 있습니다.");
        }
        if (isWithdrawExpired()) {
            throw new DomainValidationException("탈퇴 유예 기간이 만료되어 복구할 수 없습니다.");
        }

        this.status = MemberStatus.ACTIVE;
        this.withdrawnAt = null;
        this.withdrawExpireAt = null;
    }

    public boolean isWithdrawExpired() {
        return isWithdrawn()
                && withdrawExpireAt != null
                && LocalDateTime.now().isAfter(withdrawExpireAt);
    }

    public long getWithdrawRemainingDays() {
        if (!isWithdrawn() || withdrawExpireAt == null) {
            return 0L;
        }

        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), withdrawExpireAt);
        return Math.max(days, 0L);
    }

    public boolean canLogin() {
        return isActive() || isSuspended() || isWithdrawn();
    }

    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == MemberStatus.SUSPENDED;
    }

    public boolean isWithdrawn() {
        return this.status == MemberStatus.WITHDRAWN;
    }

    public boolean isDeleted() {
        return this.status == MemberStatus.DELETED;
    }

    private void validateUsername(String username) {
        validateText(username, "아이디", USERNAME_MAX_LENGTH);

        if (username.length() < MemberValidationPolicy.USERNAME_MIN_LENGTH) {
            throw new DomainValidationException(
                    "아이디는 " + MemberValidationPolicy.USERNAME_MIN_LENGTH + "자 이상이어야 합니다."
            );
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new DomainValidationException(
                    "아이디는 영문 소문자, 숫자, 밑줄(_), 마침표(.)만 사용할 수 있으며 시작과 끝은 영문 소문자 또는 숫자여야 합니다."
            );
        }

        if (username.contains("..")) {
            throw new DomainValidationException("아이디에 연속된 마침표(..)는 사용할 수 없습니다.");
        }

        if (username.contains("@")) {
            throw new DomainValidationException("아이디는 이메일 형식으로 사용할 수 없습니다.");
        }
    }

    private void validatePassword(String password) {
        validateText(password, "비밀번호", PASSWORD_MAX_LENGTH);
    }

    private void validateNickname(String nickname) {
        validateText(nickname, "닉네임", NICKNAME_MAX_LENGTH);

        if (nickname.length() < MemberValidationPolicy.NICKNAME_MIN_LENGTH) {
            throw new DomainValidationException(
                    "닉네임은 " + MemberValidationPolicy.NICKNAME_MIN_LENGTH + "자 이상이어야 합니다."
            );
        }

        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new DomainValidationException("닉네임은 한글, 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.");
        }

        if (nickname.chars().allMatch(Character::isDigit)) {
            throw new DomainValidationException("닉네임은 숫자만으로 만들 수 없습니다.");
        }

        String lowerNickname = nickname.toLowerCase();
        for (String forbiddenPrefix : MemberValidationPolicy.FORBIDDEN_NICKNAME_PREFIXES) {
            if (lowerNickname.startsWith(forbiddenPrefix)) {
                throw new DomainValidationException("사용할 수 없는 닉네임 형식입니다.");
            }
        }

        for (String forbiddenExact : MemberValidationPolicy.FORBIDDEN_NICKNAME_EXACT) {
            if (forbiddenExact.equalsIgnoreCase(nickname)) {
                throw new DomainValidationException("사용할 수 없는 닉네임입니다.");
            }
        }
    }

    private void validateSystemForcedNickname(String nickname) {
        validateText(nickname, "닉네임", NICKNAME_MAX_LENGTH);

        if (nickname.length() < MemberValidationPolicy.NICKNAME_MIN_LENGTH) {
            throw new DomainValidationException(
                    "닉네임은 " + MemberValidationPolicy.NICKNAME_MIN_LENGTH + "자 이상이어야 합니다."
            );
        }

        if (!nickname.matches("^[A-Za-z0-9_]{2,12}$")) {
            throw new DomainValidationException("관리자 강제 닉네임 형식이 올바르지 않습니다.");
        }
    }

    private void validateEmail(String email) {
        validateText(email, "이메일", EMAIL_MAX_LENGTH);

        if (email.length() < MemberValidationPolicy.EMAIL_MIN_LENGTH) {
            throw new DomainValidationException(
                    "이메일은 " + MemberValidationPolicy.EMAIL_MIN_LENGTH + "자 이상이어야 합니다."
            );
        }

        if (email.contains(" ")) {
            throw new DomainValidationException("이메일에는 공백을 포함할 수 없습니다.");
        }

        if (!email.contains("@")) {
            throw new DomainValidationException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validateRole(MemberRole role) {
        if (role == null) {
            throw new DomainValidationException("회원 역할은 필수입니다.");
        }
    }

    private void validateStatus(MemberStatus status) {
        if (status == null) {
            throw new DomainValidationException("회원 상태는 필수입니다.");
        }
    }

    private void validateText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(fieldName + "은(는) 비어 있을 수 없습니다.");
        }
        if (!value.equals(value.trim())) {
            throw new DomainValidationException(fieldName + "의 앞뒤 공백은 허용되지 않습니다.");
        }
        if (value.length() > maxLength) {
            throw new DomainValidationException(fieldName + "은(는) " + maxLength + "자를 초과할 수 없습니다.");
        }
    }

    public boolean hasNicknameChangedBefore() {
        return this.nicknameChangedAt != null;
    }

    public boolean isSameNickname(String nickname) {
        if (nickname == null) {
            return false;
        }
        return this.nickname.equals(nickname);
    }

    public boolean isFinalizedBefore(LocalDateTime cutoffDateTime) {
        return this.finalizedAt != null && this.finalizedAt.isBefore(cutoffDateTime);
    }

    public void forceChangeNickname(String nickname) {
        validateSystemForcedNickname(nickname);
        this.nickname = nickname;
    }
}