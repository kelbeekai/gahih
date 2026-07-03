package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.*;
import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.service.status.MemberFinalizationService;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.service.ReportResolutionService;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AdminLogRepository adminLogRepository;
    private final MemberFinalizationService memberFinalizationService;
    private final ReportResolutionService resolutionService;

    public List<AdminMemberResponse> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(AdminMemberResponse::from)
                .toList();
    }

    public Page<AdminMemberResponse> searchMembers(AdminMemberSearchCondition condition) {
        return memberRepository.searchAdminMemberPage(
                condition.getKeywordOrNull(),
                condition.getSearchType(),
                condition.getRole(),
                condition.getStatus(),
                condition.getSortName(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public AdminMemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        List<AdminMemberDetailPostResponse> recentPosts = postRepository
                .findTop5ByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(AdminMemberDetailPostResponse::from)
                .toList();

        List<AdminMemberDetailCommentResponse> recentComments = commentRepository
                .findTop5ByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(AdminMemberDetailCommentResponse::from)
                .toList();

        return AdminMemberDetailResponse.from(member, recentPosts, recentComments);
    }

    @Transactional
    public void updateMemberRole(Long adminMemberId, Long targetMemberId, MemberRole newRole) {
        Member admin = getAdmin(adminMemberId);
        Member target = getMember(targetMemberId);

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException("본인의 권한은 관리자 페이지에서 변경할 수 없습니다.");
        }

        if (target.isWithdrawn() || target.isDeleted()) {
            throw new BusinessException("탈퇴 또는 삭제된 회원의 권한은 변경할 수 없습니다.");
        }

        String beforeSnapshot = buildMemberSnapshot(target);

        switch (newRole) {
            case ADMIN -> target.promoteToAdmin();
            case USER -> target.demoteToUser();
            default -> throw new BusinessException("변경할 수 없는 회원 권한입니다.");
        }

        String afterSnapshot = buildMemberSnapshot(target);

//        saveLog(admin, "MEMBER_ROLE_CHANGED_TO_" + newRole.name(), target.getId());
//        saveLog(admin, "MEMBER_ROLE_CHANGED_TO_" + newRole.name(), target.getId(), target.getNickname());

        saveLog(
                admin,
                "MEMBER_ROLE_CHANGED_TO_" + newRole.name(),
                AdminLogTargetType.MEMBER,
                target.getId(),
                target.getNickname(),
                null,
                null,
                null,
                beforeSnapshot,
                afterSnapshot
        );
    }

    @Transactional
    public void activateMember(Long adminMemberId, Long targetMemberId) {
        Member admin = getAdmin(adminMemberId);
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException("본인의 회원 상태는 변경할 수 없습니다.");
        }

        if (target.isWithdrawn()) {
            throw new BusinessException("탈퇴 유예 중인 회원을 임의로 활성화시킬 수 없습니다.");
        }

        if (target.isDeleted()) {
            throw new BusinessException("최종 종료는 전용 기능을 통해서만 처리할 수 있습니다.");
        }


        String beforeSnapshot = buildMemberSnapshot(target);

        target.activate();

        String afterSnapshot = buildMemberSnapshot(target);

//        saveLog(admin, "MEMBER_STATUS_CHANGED_TO_" + newStatus.name(), target.getId());
//        saveLog(admin, "MEMBER_STATUS_CHANGED_TO_" + newStatus.name(), target.getId(), target.getNickname());

        saveLog(
                admin,
                "ACTIVATE_MEMBER",
                AdminLogTargetType.MEMBER,
                target.getId(),
                target.getNickname(),
                null,
                null,
                null,
                beforeSnapshot,
                afterSnapshot
        );
    }

    @Transactional
    public void suspendMemberTemporarily(
            Long adminMemberId,
            Long targetMemberId,
            java.time.LocalDateTime suspendedUntil,
            String reason
    ) {
        Member admin = getAdmin(adminMemberId);
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException("본인의 회원 상태는 변경할 수 없습니다.");
        }

        if (target.isDeleted()) {
            throw new BusinessException("최종 종료된 회원은 기간정지할 수 없습니다.");
        }

        if (target.isAdmin()) {
            throw new BusinessException("관리자 계정은 기간정지할 수 없습니다.");
        }

        String beforeSnapshot = buildMemberSnapshot(target);
        String normalizedReason = requireReason(reason, "기간정지 사유");

        target.suspendTemporarily(suspendedUntil, normalizedReason);

        unpinAllPinnedPostsOfMember(target.getId());

        resolutionService.resolveByActionIfPresent(
                ReportTargetType.MEMBER,
                target.getId(),
                admin.getId(),
                normalizedReason
        );

        String afterSnapshot = buildMemberSnapshot(target);

        saveLog(
                admin,
                "TEMPORARY_SUSPEND_MEMBER",
                AdminLogTargetType.MEMBER,
                target.getId(),
                target.getNickname(),
                null,
                null,
                normalizedReason,
                beforeSnapshot,
                afterSnapshot
        );
    }

    @Transactional
    public void suspendMemberPermanently(
            Long adminMemberId,
            Long targetMemberId,
            String reason
    ) {
        Member admin = getAdmin(adminMemberId);
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException("본인의 회원 상태는 변경할 수 없습니다.");
        }

        if (target.isDeleted()) {
            throw new BusinessException("최종 종료된 회원은 영구정지할 수 없습니다.");
        }

        if (target.isAdmin()) {
            throw new BusinessException("관리자 계정은 영구정지할 수 없습니다.");
        }

        String beforeSnapshot = buildMemberSnapshot(target);
        String normalizedReason = requireReason(reason, "영구정지 사유");

        target.suspendPermanently(normalizedReason);

        unpinAllPinnedPostsOfMember(target.getId());

        resolutionService.resolveByActionIfPresent(
                ReportTargetType.MEMBER,
                target.getId(),
                admin.getId(),
                normalizedReason
        );

        String afterSnapshot = buildMemberSnapshot(target);

        saveLog(
                admin,
                "PERMANENT_SUSPEND_MEMBER",
                AdminLogTargetType.MEMBER,
                target.getId(),
                target.getNickname(),
                null,
                null,
                normalizedReason,
                beforeSnapshot,
                afterSnapshot
        );
    }

    @Transactional
    public void finalizeMember(Long adminMemberId, Long targetMemberId, String reason) {
        Member admin = getAdmin(adminMemberId);
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (admin.getId().equals(target.getId())) {
            throw new BusinessException("자기 자신은 최종 종료할 수 없습니다.");
        }

        if (target.isDeleted()) {
            throw new BusinessException("이미 최종 종료 처리된 회원입니다.");
        }

        if (target.getRole() == MemberRole.ADMIN) { // target.isAdmin()
            throw new BusinessException("관리자 계정은 최종 종료할 수 없습니다.");
        }

        String originalNickname = target.getNickname();
        String beforeSnapshot = buildMemberSnapshot(target);

        memberFinalizationService.finalizeMember(target);

        String afterSnapshot = buildMemberSnapshot(target);

        String normalizedReason = requireReason(reason, "최종 종료 사유");

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.MEMBER,
                target.getId(),
                admin.getId(),
                normalizedReason
        );

//        saveLog(admin, "FINALIZE_MEMBER", target.getId(), originalNickname);

        saveLog(
                admin,
                "FINALIZE_MEMBER",
                AdminLogTargetType.MEMBER,
                target.getId(),
                originalNickname,
                null,
                null,
                normalizedReason,
                beforeSnapshot,
                afterSnapshot
        );

        anonymizeFinalizedMemberLogs(target.getId(), originalNickname);
    }

    private void anonymizeFinalizedMemberLogs(Long memberId, String originalNickname) {
        List<AdminLog> logs = adminLogRepository.findAllByTargetTypeAndTargetId(AdminLogTargetType.MEMBER, memberId);

        for (AdminLog log : logs) {
            log.anonymizeMemberNicknameData(originalNickname);
        }
    }

    private Member getAdmin(Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 관리자입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new UnauthorizedException("관리자 권한이 없습니다.");
        }

        return admin;
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    private void saveLog(
            Member admin,
            String action,
            AdminLogTargetType targetType,
            Long targetId,
            String targetName,
            String targetCommunityCode,
            String targetCommunityName,
            String reason,
            String beforeSnapshot,
            String afterSnapshot
    ) {
        AdminLog adminLog = AdminLog.create(
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
        adminLogRepository.save(adminLog);

        log.info(
                "Admin action completed. action={}, adminId={}, targetType={}, targetId={}, communityCode={}",
                action,
                admin.getId(),
                targetType,
                targetId,
                targetCommunityCode
        );
    }

    private String buildMemberSnapshot(Member member) {
        return "username=" + member.getUsername()
                + ", nickname=" + member.getNickname()
                + ", email=" + member.getEmail()
                + ", role=" + member.getRole().name()
                + ", status=" + member.getStatus().name();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }

    private String requireReason(String reason, String label) {
        String normalized = normalizeReason(reason);
        if (normalized == null) {
            throw new BusinessException(label + "를 입력해주세요.");
        }
        return normalized;
    }

    // 회원의 고정글 모두 해제
    private void unpinAllPinnedPostsOfMember(Long memberId) {
        List<Post> pinnedPosts = postRepository.findAllByMemberIdAndPinnedTrue(memberId);

        for (Post post : pinnedPosts) {
            post.unpin();
        }
    }

}
