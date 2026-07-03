package com.gahih.domain.member.service;

import com.gahih.domain.member.dto.MemberSuspendedResponse;
import com.gahih.domain.member.dto.MemberWithdrawnResponse;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberStatusService {

    private final MemberRepository memberRepository;
    private final MemberFinalizationService memberFinalizationService;

    /**
     * 탈퇴 유예 기간 종료 후 WITHDRAWN -> DELETED 자동 정리 메서드
     */
    @Transactional
    public int finalizeExpiredWithdrawnMembers() {
        List<Member> expiredWithdrawnMembers = memberRepository
                .findAllByStatusAndWithdrawExpireAtBefore(MemberStatus.WITHDRAWN, LocalDateTime.now());

        int finalizedCount = 0;

        for (Member member : expiredWithdrawnMembers) {
            memberFinalizationService.finalizeMember(member);
            finalizedCount++;
        }

        return finalizedCount;
    }

    @Transactional
    public int releaseExpiredSuspendedMembers() {
        List<Member> suspendedMembers = memberRepository.findAllByStatus(MemberStatus.SUSPENDED);

        int releasedCount = 0;

        for (Member member : suspendedMembers) {
            if (member.isSuspensionExpired()) {
                member.releaseSuspension();
                releasedCount++;
            }
        }

        return releasedCount;
    }

    @Transactional
    public void restoreWithdrawnMember(Long loginMemberId) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        member.restoreFromWithdraw();

        log.info(
                "Withdrawn member restored. memberId={}, username={}",
                member.getId(),
                member.getUsername()
        );
    }

    public MemberWithdrawnResponse getWithdrawnInfo(Long loginMemberId) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (!member.isWithdrawn()) {
            throw new BusinessException("탈퇴 유예 상태의 회원만 접근할 수 있습니다.");
        }

        return MemberWithdrawnResponse.from(member);
    }

    public MemberSuspendedResponse getSuspendedInfo(Long loginMemberId) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (!member.isSuspended()) {
            throw new BusinessException("정지 상태의 회원만 접근할 수 있습니다.");
        }

        if (member.isSuspensionExpired()) {
            member.releaseSuspension();
            throw new BusinessException("정지 기간이 만료되어 정상적으로 이용할 수 있습니다.");
        }

        return MemberSuspendedResponse.from(member);
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

}
