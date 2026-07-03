package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.AdminCommentResponse;
import com.gahih.domain.admin.dto.AdminCommentSearchCondition;
import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.repository.CommentReactionRepository;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final AdminLogRepository adminLogRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final ReportResolutionService resolutionService;

    public Page<AdminCommentResponse> searchComments(AdminCommentSearchCondition condition) {
        return commentRepository.searchAdminCommentPage(
                condition.getCommunityCode(),
                condition.getKeywordOrNull(),
                condition.getSearchType(),
                condition.getStatus(),
                condition.getSortName(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    @Transactional
    public void blindComment(Long adminMemberId, Long commentId) {
        Member admin = getAdmin(adminMemberId);
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        String beforeSnapshot = buildCommentSnapshot(comment);

        comment.blindByAdmin();

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.COMMENT,
                comment.getId(),
                admin.getId(),
                "관리자 댓글 블라인드 조치"
        );

        String afterSnapshot = buildCommentSnapshot(comment);

//        saveLog(admin, "BLIND_COMMENT", comment.getId(), trimContent(comment.getContent()));

        saveLog(admin,
                "BLIND_COMMENT",
                AdminLogTargetType.COMMENT,
                comment.getId(),
                trimContent(comment.getContent()),
                comment.getPost().getCategory().getCountryCommunity().getCode(),
                comment.getPost().getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot);
    }

    @Transactional
    public void deleteComment(Long adminMemberId, Long commentId) {
        Member admin = getAdmin(adminMemberId);
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        String beforeSnapshot = buildCommentSnapshot(comment);

        comment.deleteByAdmin();

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.COMMENT,
                comment.getId(),
                admin.getId(),
                "관리자 댓글 삭제 조치"
        );

        String afterSnapshot = buildCommentSnapshot(comment);

//        saveLog(admin, "DELETE_COMMENT", comment.getId(), trimContent(comment.getContent()));

        saveLog(admin,
                "DELETE_COMMENT",
                AdminLogTargetType.COMMENT,
                comment.getId(),
                trimContent(comment.getContent()),
                comment.getPost().getCategory().getCountryCommunity().getCode(),
                comment.getPost().getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot);
    }

    /**
     * 관리자 댓글 복구 메서드
     */
    @Transactional
    public void restoreComment(Long adminMemberId, Long commentId) {
        Member admin = getAdmin(adminMemberId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        String beforeSnapshot = buildCommentSnapshot(comment);

        comment.restoreByAdmin();


//        saveLog(admin, "RESTORE_COMMENT", comment.getId(), trimContent(comment.getContent()));

        String afterSnapshot = buildCommentSnapshot(comment);

        saveLog(admin,
                "RESTORE_COMMENT",
                AdminLogTargetType.COMMENT,
                comment.getId(),
                trimContent(comment.getContent()),
                comment.getPost().getCategory().getCountryCommunity().getCode(),
                comment.getPost().getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot);
    }

    /**
     * 관리자 댓글 영구 삭제 메서드
     */
    @Transactional
    public void hardDeleteComment(Long adminMemberId, Long commentId, String reason) {
        Member admin = getAdmin(adminMemberId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        if (!comment.isHardDeletableByAdmin()) {
            throw new BusinessException("관리자 삭제 처리된 댓글만 영구 삭제할 수 있습니다.");
        }

        String targetName = trimContent(comment.getContent());
        String beforeSnapshot = buildCommentSnapshot(comment);

        String communityCode = comment.getPost().getCategory().getCountryCommunity().getCode();
        String communityName = comment.getPost().getCategory().getCountryCommunity().getName();

        commentReactionRepository.deleteAllByCommentId(commentId);
        commentRepository.delete(comment);

        String afterSnapshot = buildCommentSnapshot(comment);

        String normalizedReason = requireReason(reason, "댓글 영구삭제 사유");

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.COMMENT,
                commentId,
                admin.getId(),
                normalizedReason
        );

//        saveLog(admin, "HARD_DELETE_COMMENT", commentId, targetName);

        saveLog(admin,
                "HARD_DELETE_COMMENT",
                AdminLogTargetType.COMMENT,
                commentId,
                targetName,
                communityCode,
                communityName,
                normalizedReason,
                beforeSnapshot,
                afterSnapshot);
    }

    private Member getAdmin(Long adminMemberId) {
        Member admin = memberRepository.findById(adminMemberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 관리자입니다."));

        if (admin.getRole() != MemberRole.ADMIN) {
            throw new UnauthorizedException("관리자 권한이 없습니다.");
        }

        return admin;
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

    private String buildCommentSnapshot(Comment comment) {
        return "content=" + trimContent(comment.getContent())
                + ", community=" + comment.getPost().getCategory().getCountryCommunity().getName()
                + ", postId=" + comment.getPost().getId()
                + ", status=" + comment.getStatus().name();
    }

    private String trimContent(String content) {
        if (content == null || content.isBlank()) {
            return "(내용 없음)";
        }
        return content.length() > 30 ? content.substring(0, 30) + "..." : content;
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
}
