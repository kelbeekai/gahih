package com.gahih.domain.member.service;

import com.gahih.domain.member.dto.MemberMyPageResponse;
import com.gahih.domain.member.dto.MemberPasswordChangeRequest;
import com.gahih.domain.member.dto.MemberPasswordResetRequest;
import com.gahih.domain.member.dto.MemberUpdateRequest;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.entity.NicknameHistory;
import com.gahih.domain.member.entity.NicknameReservation;
import com.gahih.domain.member.policy.MemberValidationPolicy;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.repository.NicknameHistoryRepository;
import com.gahih.domain.member.repository.NicknameReservationRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DuplicateResourceException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAccountService {

    private static final long NICKNAME_CHANGE_COOLDOWN_DAYS = 30;
    private static final long NICKNAME_REUSE_BLOCK_DAYS = 90;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameHistoryRepository nicknameHistoryRepository;
    private final NicknameReservationRepository nicknameReservationRepository;
    private final EmailChangeAuthFacade emailChangeAuthFacade;
    private final PostRepository postRepository;

    public MemberMyPageResponse getMyPage(Long loginMemberId) {
        Member member = getMyPageAccessibleMember(loginMemberId);
        return MemberMyPageResponse.from(member);
    }

    public MemberUpdateRequest getMemberUpdateForm(Long loginMemberId) {
        Member member = getMyPageAccessibleMember(loginMemberId);

        MemberUpdateRequest request = new MemberUpdateRequest();
        request.setNickname(member.getNickname());
        request.setEmail(member.getEmail());
        return request;
    }

    @Transactional
    public void updateMember(Long loginMemberId, MemberUpdateRequest request) {
        Member member = getMyPageAccessibleMember(loginMemberId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new UnauthorizedException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newNickname = normalizeNickname(request.getNickname());
        String newEmail = normalizeEmail(request.getEmail());

        boolean nicknameChanged = !member.isSameNickname(newNickname);
        boolean emailChanged = !member.getEmail().equals(newEmail);

        if (nicknameChanged) {
            String previousNickname = member.getNickname();

            validateNicknameChangeAllowed(member, newNickname);
            member.changeNickname(newNickname);
            saveUserNicknameChangeHistory(member, previousNickname, newNickname);
            saveNicknameReservationForUserChange(previousNickname);
        }

        if (emailChanged) {
            Member existingMember = memberRepository.findByEmail(newEmail).orElse(null);
            if (existingMember != null && !existingMember.getId().equals(loginMemberId)) {
                throw new DuplicateResourceException("해당 이메일은 사용할 수 없습니다."); // 다른 회원의 이메일로 변경 시도 차단 방어
            }

            emailChangeAuthFacade.validateVerifiedOrThrow(newEmail, loginMemberId);
            member.changeEmail(newEmail);
        }
    }

    @Transactional
    public void changePassword(Long loginMemberId, MemberPasswordChangeRequest request) {
        Member member = getMyPageAccessibleMember(loginMemberId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new UnauthorizedException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }


        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new BusinessException("새 비밀번호는 기존 비밀번호와 다르게 설정해주세요.");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.changePassword(encodedPassword);
    }

    @Transactional
    public void resetPasswordByRecovery(Long memberId, MemberPasswordResetRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (member.isDeleted()) {
            throw new BusinessException("비밀번호를 재설정할 수 없는 회원 상태입니다.");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new BusinessException("새 비밀번호는 기존 비밀번호와 다르게 설정해주세요.");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.changePassword(encodedPassword);
    }

    /**
     * 회원 탈퇴 메서드
     */
    @Transactional
    public void withdraw(Long loginMemberId, String password) {
        Member member = getMyPageAccessibleMember(loginMemberId);

        if (!member.isActive()) {
            throw new UnauthorizedException("현재 회원 상태에서는 탈퇴할 수 없습니다.");
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }

        unpinAllPinnedPostsOfMember(loginMemberId);
        member.withdraw();

        log.info(
                "Member withdrawal started. memberId={}, username={}",
                member.getId(),
                member.getUsername()
        );
    }

    public boolean isWithdrawAllowed(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        return member.isActive();
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    /**
     * ACTIVE, SUSPENDED: 마이페이지 가능, WITHDRAWN: withdrawn 화면만, DELETED: 불가
     */
    private Member getMyPageAccessibleMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (!(member.isActive() || member.isSuspended())) {
            throw new UnauthorizedException("해당 회원 상태에서는 마이페이지를 이용할 수 없습니다.");
        }

        return member;
    }

    private void validateNicknameChangeAllowed(Member member, String newNickname) {
        validateNicknameDuplicate(newNickname);
        validateNicknameCooldown(member);
        validateNicknameReuseBlocked(newNickname);
    }

    private void validateNicknameDuplicate(String newNickname) {
        if (memberRepository.existsByNickname(newNickname)) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
    }

    private void validateNicknameCooldown(Member member) {
        if (!member.hasNicknameChangedBefore()) {
            return;
        }

        LocalDateTime availableAt = member.getNicknameChangedAt().plusDays(NICKNAME_CHANGE_COOLDOWN_DAYS);
        if (LocalDateTime.now().isBefore(availableAt)) {
            throw new BusinessException(
                    "닉네임은 " + NICKNAME_CHANGE_COOLDOWN_DAYS + "일에 한 번만 변경할 수 있습니다."
            );
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

    private void saveUserNicknameChangeHistory(Member member, String previousNickname, String newNickname) {
        NicknameHistory nicknameHistory = NicknameHistory.createUserChange(member, previousNickname, newNickname);
        nicknameHistoryRepository.save(nicknameHistory);
    }

    private void saveNicknameReservationForUserChange(String nickname) {
        NicknameReservation nicknameReservation =
                NicknameReservation.reserveForNicknameChange(nickname, NICKNAME_REUSE_BLOCK_DAYS);
        nicknameReservationRepository.save(nicknameReservation);
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

    private void unpinAllPinnedPostsOfMember(Long memberId) {
        List<Post> pinnedPosts = postRepository.findAllByMemberIdAndPinnedTrue(memberId);

        for (Post post : pinnedPosts) {
            post.unpin();
        }
    }

}
