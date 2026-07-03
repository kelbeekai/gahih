package com.gahih.domain.report.service;

import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.report.entity.ReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.repository.ReportRepository;
import com.gahih.domain.report.repository.ReportedTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportPermissionService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final ReportRepository reportRepository;
    private final ReportedTargetRepository reportedTargetRepository;

    public boolean canReportMember(Long reporterId, Long targetMemberId) {
        if (reporterId == null || targetMemberId == null) {
            return false;
        }

        Member reporter = memberRepository.findById(reporterId).orElse(null);
        Member targetMember = memberRepository.findById(targetMemberId).orElse(null);

        if (reporter == null || targetMember == null) {
            return false;
        }

        if (reporter.getId().equals(targetMember.getId())) {
            return false;
        }

        if (targetMember.getRole() == MemberRole.ADMIN) {
            return false;
        }

        if (targetMember.isDeleted()) {
            return false;
        }

        return !isAlreadyReportedCurrentCycle(reporterId, ReportTargetType.MEMBER, targetMemberId);
    }

    public boolean canReportPost(Long reporterId, Long postId) {
        if (reporterId == null || postId == null) {
            return false;
        }

        Member reporter = memberRepository.findById(reporterId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);

        if (reporter == null || post == null) {
            return false;
        }

        if (reporter.getId().equals(post.getMember().getId())) {
            return false;
        }

        if (post.getMember().getRole() == MemberRole.ADMIN) {
            return false;
        }

        if (!post.isActive()) {
            return false;
        }

        return !isAlreadyReportedCurrentCycle(reporterId, ReportTargetType.POST, postId);
    }

    public boolean canReportComment(Long reporterId, Long commentId) {
        if (reporterId == null || commentId == null) {
            return false;
        }

        Member reporter = memberRepository.findById(reporterId).orElse(null);
        Comment comment = commentRepository.findById(commentId).orElse(null);

        if (reporter == null || comment == null) {
            return false;
        }

        if (reporter.getId().equals(comment.getMember().getId())) {
            return false;
        }

        if (comment.getMember().getRole() == MemberRole.ADMIN) {
            return false;
        }

        if (!comment.isActive()) {
            return false;
        }

        return !isAlreadyReportedCurrentCycle(reporterId, ReportTargetType.COMMENT, commentId);
    }

    public boolean canReportAttachment(Long reporterId, Long attachmentId) {
        if (reporterId == null || attachmentId == null) {
            return false;
        }

        Member reporter = memberRepository.findById(reporterId).orElse(null);
        PostAttachment attachment = postAttachmentRepository.findById(attachmentId).orElse(null);

        if (reporter == null || attachment == null) {
            return false;
        }

        if (reporter.getId().equals(attachment.getPost().getMember().getId())) {
            return false;
        }

        if (attachment.getPost().getMember().getRole() == MemberRole.ADMIN) {
            return false;
        }

        if (!attachment.isActive() || !attachment.getPost().isActive()) {
            return false;
        }

        return !isAlreadyReportedCurrentCycle(reporterId, ReportTargetType.ATTACHMENT, attachmentId);
    }

    private boolean isAlreadyReportedCurrentCycle(Long reporterId, ReportTargetType targetType, Long targetId) {
        int currentCycle = reportedTargetRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .map(ReportedTarget::getCurrentCycle)
                .orElse(1);

        return reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndReportCycle(
                reporterId,
                targetType,
                targetId,
                currentCycle
        );
    }

}
