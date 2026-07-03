package com.gahih.domain.member.service.auth;

import com.gahih.domain.member.dto.MemberLoginRequest;
import com.gahih.domain.member.dto.MemberSignUpRequest;
import com.gahih.domain.member.dto.MemberSignUpResponse;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.entity.NicknameHistory;
import com.gahih.domain.member.policy.MemberValidationPolicy;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.repository.NicknameHistoryRepository;
import com.gahih.domain.member.repository.NicknameReservationRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DuplicateResourceException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAuthService {

    private static final long NICKNAME_REUSE_BLOCK_DAYS = 90;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameHistoryRepository nicknameHistoryRepository;
    private final SignUpEmailAuthFacade signUpEmailAuthFacade;
    private final NicknameReservationRepository nicknameReservationRepository;


    @Transactional
    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedNickname = normalizeNickname(request.getNickname());
        String normalizedEmail = normalizeEmail(request.getEmail());

        validatePasswordConfirm(request.getPassword(), request.getPasswordConfirm());
        validateDuplicate(normalizedUsername, normalizedNickname, normalizedEmail);
        signUpEmailAuthFacade.validateVerifiedOrThrow(normalizedEmail);

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.createUser(
                normalizedUsername,
                encodedPassword,
                normalizedNickname,
                normalizedEmail
        );

        Member savedMember = memberRepository.save(member);

        log.info(
                "Member signup completed. memberId={}, username={}",
                savedMember.getId(),
                savedMember.getUsername()
        );

        saveInitialNicknameHistory(savedMember);

        return MemberSignUpResponse.from(savedMember);
    }

    public Member login(MemberLoginRequest request) {
        String normalizedUsername = normalizeUsername(request.getLoginId());

        Member member = memberRepository
                .findByUsername(normalizedUsername)
                .orElseThrow(() -> {
                    log.warn("Login failed. username not found. username={}", normalizedUsername);
                    return new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
                });

        boolean matches = passwordEncoder.matches(request.getPassword(), member.getPassword());
        if (!matches) {

            log.warn(
                    "Login failed. invalid password. memberId={}, username={}",
                    member.getId(),
                    member.getUsername()
            );

            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        validateLoginAvailable(member);

        log.info(
                "Member login succeeded. memberId={}, username={}",
                member.getId(),
                member.getUsername()
        );

        return member;
    }

    private void validateDuplicate(String username, String nickname, String email) {
        if (memberRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("이미 사용 중인 아이디입니다.");
        }

        validateNicknameDuplicate(nickname);
        validateNicknameReuseBlocked(nickname);

        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다.");
        }
    }

    /**
     * 회원 상태 검사 로그인 메서드 구현 시 활성화
     */
    private void validateLoginAvailable(Member member) {
        if (member.isDeleted()) {

            log.warn(
                    "Blocked login attempt. memberId={}, status={}",
                    member.getId(),
                    member.getStatus()
            );

            throw new UnauthorizedException("삭제 처리된 회원입니다.");
        }

        if (member.isWithdrawn() && member.isWithdrawExpired()) {

            log.warn(
                    "Blocked login attempt. memberId={}, status={}",
                    member.getId(),
                    member.getStatus()
            );

            throw new UnauthorizedException("탈퇴 유예 기간이 만료된 회원입니다.");
        }

        if (member.isSuspensionExpired()) {
            member.releaseSuspension();
        }

        if (!member.canLogin()) {

            log.warn(
                    "Blocked login attempt. memberId={}, status={}",
                    member.getId(),
                    member.getStatus()
            );

            throw new UnauthorizedException("로그인할 수 없는 회원 상태입니다.");
        }

    }

    private void validateNicknameDuplicate(String newNickname) {
        if (memberRepository.existsByNickname(newNickname)) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
    }

    private void validateNicknameReuseBlocked(String newNickname) {
        boolean blocked = nicknameReservationRepository
                .existsByNicknameIgnoreCaseAndExpiresAtAfter(newNickname, LocalDateTime.now());

        if (blocked) {
            throw new BusinessException(
                    "최근 " + NICKNAME_REUSE_BLOCK_DAYS + "일 이내에 사용된 닉네임은 재사용할 수 없습니다."
            );
        }
    }

    private void validatePasswordConfirm(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new BusinessException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    private void saveInitialNicknameHistory(Member member) {
        NicknameHistory nicknameHistory = NicknameHistory.createInitial(member, member.getNickname());
        nicknameHistoryRepository.save(nicknameHistory);
    }

    private String normalizeUsername(String username) {
        String normalized = username.trim().toLowerCase(Locale.ROOT);

        if (normalized.contains("..")) {
            throw new BusinessException("아이디에 연속된 마침표(..)는 사용할 수 없습니다.");
        }

        if (normalized.contains("@")) {
            throw new BusinessException("아이디는 이메일 형식으로 사용할 수 없습니다.");
        }

        return normalized;
    }

    private String normalizeNickname(String nickname) {
        String normalized = nickname.trim();

        if (normalized.chars().allMatch(Character::isDigit)) {
            throw new BusinessException("닉네임은 숫자만으로 만들 수 없습니다.");
        }

        String lowerNickname = normalized.toLowerCase(Locale.ROOT);
        for (String forbiddenPrefix : MemberValidationPolicy.FORBIDDEN_NICKNAME_PREFIXES) {
            if (lowerNickname.startsWith(forbiddenPrefix)) {
                throw new BusinessException("사용할 수 없는 닉네임 형식입니다.");
            }
        }

        for (String forbiddenExact : MemberValidationPolicy.FORBIDDEN_NICKNAME_EXACT) {
            if (forbiddenExact.equalsIgnoreCase(normalized)) {
                throw new BusinessException("사용할 수 없는 닉네임입니다.");
            }
        }

        return normalized;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
