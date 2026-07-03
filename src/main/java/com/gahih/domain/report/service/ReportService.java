package com.gahih.domain.report.service;

import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.community.entity.CountryCommunity;
import com.gahih.domain.community.repository.CountryCommunityRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.report.dto.*;
import com.gahih.domain.report.entity.Report;
import com.gahih.domain.report.entity.ReportedTarget;
import com.gahih.domain.report.enumtype.ReportTargetType;
import com.gahih.domain.report.repository.ReportRepository;
import com.gahih.domain.report.repository.ReportedTargetRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportedTargetRepository reportedTargetRepository;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostAttachmentRepository postAttachmentRepository;

    private final CountryCommunityRepository countryCommunityRepository;

    @Transactional
    public void createReport(Long reporterId, ReportCreateRequest request, String requestCommunityCode) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        TargetSnapshot snapshot = resolveTargetSnapshot(reporter, request.getTargetType(), request.getTargetId());

        ReportCommunitySnapshot reportCommunitySnapshot = resolveReportCommunitySnapshot(
                request.getTargetType(),
                snapshot,
                requestCommunityCode
        );

        ReportedTarget reportedTarget = reportedTargetRepository
                .findByTargetTypeAndTargetId(request.getTargetType(), request.getTargetId())
                .orElse(null);

        int currentCycle = (reportedTarget == null) ? 1 : reportedTarget.getCurrentCycle();

        boolean duplicated = reportRepository.existsByReporterIdAndTargetTypeAndTargetIdAndReportCycle(
                reporterId,
                request.getTargetType(),
                request.getTargetId(),
                currentCycle
        );

        if (duplicated) {

            log.warn(
                    "Duplicate report blocked. reporterId={}, targetType={}, targetId={}",
                    reporter.getId(),
                    request.getTargetType(),
                    request.getTargetId()
            );

            throw new BusinessException("이미 신고가 완료되었습니다.");
        }

        Report report = Report.create(
                reporter,
                request.getTargetType(),
                request.getTargetId(),
                currentCycle,
                request.getReasonType(),
                request.getDetail(),
                reportCommunitySnapshot.communityCode(),
                reportCommunitySnapshot.communityName()
        );
        reportRepository.save(report);

        log.info(
                "Report submitted. reporterId={}, targetType={}, targetId={}",
                reporter.getId(),
                request.getTargetType(),
                request.getTargetId()
        );

        if (reportedTarget == null) {
            ReportedTarget newReportedTarget = ReportedTarget.createFirstReport(
                    request.getTargetType(),
                    request.getTargetId(),
                    snapshot.targetNameSnapshot(),
                    snapshot.writerMemberId(),
                    snapshot.writerNicknameSnapshot(),
                    snapshot.parentPostId(),
                    snapshot.parentPostTitleSnapshot(),
                    snapshot.communityCode(),
                    snapshot.communityName()
            );
            reportedTargetRepository.save(newReportedTarget);

            return;
        }

        reportedTarget.applyNewReport(
                snapshot.targetNameSnapshot(),
                snapshot.writerMemberId(),
                snapshot.writerNicknameSnapshot(),
                snapshot.parentPostId(),
                snapshot.parentPostTitleSnapshot(),
                snapshot.communityCode(),
                snapshot.communityName()
        );

    }

    private TargetSnapshot resolveTargetSnapshot(Member reporter, ReportTargetType targetType, Long targetId) {
        if (targetType == ReportTargetType.MEMBER) {
            Member targetMember = memberRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

            if (reporter.getId().equals(targetMember.getId())) {

                log.warn(
                        "Self report blocked. reporterId={}, targetType={}, targetId={}",
                        reporter.getId(),
                        targetType,
                        targetId
                );

                throw new BusinessException("본인은 신고할 수 없습니다.");
            }

            if (targetMember.getRole() == MemberRole.ADMIN) {
                throw new BusinessException("관리자는 신고할 수 없습니다.");
            }

            if (targetMember.isDeleted()) {
                throw new BusinessException("이미 최종 종료된 회원은 신고할 수 없습니다.");
            }

            return new TargetSnapshot(
                    targetMember.getNickname(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        if (targetType == ReportTargetType.POST) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

            if (reporter.getId().equals(post.getMember().getId())) {
                log.warn(
                        "Self report blocked. reporterId={}, targetType={}, targetId={}",
                        reporter.getId(),
                        targetType,
                        targetId
                );

                throw new BusinessException("본인의 게시글은 신고할 수 없습니다.");
            }

            if (post.getMember().getRole() == MemberRole.ADMIN) {
                throw new BusinessException("관리자가 작성한 게시글은 신고할 수 없습니다.");
            }

            if (!post.isActive()) {
                throw new BusinessException("현재 신고할 수 없는 게시글입니다.");
            }

            return new TargetSnapshot(
                    post.getTitle(),
                    post.getMember().getId(),
                    resolveWriterNickname(post.getMember()),
                    null,
                    null,
                    post.getCategory().getCountryCommunity().getCode(),
                    post.getCategory().getCountryCommunity().getName()
            );
        }

        if (targetType == ReportTargetType.COMMENT) {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

            if (reporter.getId().equals(comment.getMember().getId())) {
                log.warn(
                        "Self report blocked. reporterId={}, targetType={}, targetId={}",
                        reporter.getId(),
                        targetType,
                        targetId
                );

                throw new BusinessException("본인의 댓글은 신고할 수 없습니다.");
            }

            if (comment.getMember().getRole() == MemberRole.ADMIN) {
                throw new BusinessException("관리자가 작성한 댓글은 신고할 수 없습니다.");
            }

            if (!comment.isActive()) {
                throw new BusinessException("현재 신고할 수 없는 댓글입니다.");
            }

            return new TargetSnapshot(
                    trimText(comment.getContent(), 30),
                    comment.getMember().getId(),
                    resolveWriterNickname(comment.getMember()),
                    comment.getPost().getId(),
                    comment.getPost().getTitle(),
                    comment.getPost().getCategory().getCountryCommunity().getCode(),
                    comment.getPost().getCategory().getCountryCommunity().getName()
            );
        }

        if (targetType == ReportTargetType.ATTACHMENT) {
            PostAttachment attachment = postAttachmentRepository.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

            if (reporter.getId().equals(attachment.getPost().getMember().getId())) {
                log.warn(
                        "Self report blocked. reporterId={}, targetType={}, targetId={}",
                        reporter.getId(),
                        targetType,
                        targetId
                );

                throw new BusinessException("본인의 첨부파일은 신고할 수 없습니다.");
            }

            if (attachment.getPost().getMember().getRole() == MemberRole.ADMIN) {
                throw new BusinessException("관리자가 작성한 첨부파일은 신고할 수 없습니다.");
            }

            if (!attachment.isActive() || !attachment.getPost().isActive()) {
                throw new BusinessException("현재 신고할 수 없는 첨부파일입니다.");
            }

            return new TargetSnapshot(
                    attachment.getOriginalFileName(),
                    attachment.getPost().getMember().getId(),
                    resolveWriterNickname(attachment.getPost().getMember()),
                    attachment.getPost().getId(),
                    attachment.getPost().getTitle(),
                    attachment.getPost().getCategory().getCountryCommunity().getCode(),
                    attachment.getPost().getCategory().getCountryCommunity().getName()
            );
        }

        throw new BusinessException("지원하지 않는 신고 대상 타입입니다.");
    }

    private String resolveWriterNickname(Member member) {
        if (member.isDeleted()) {
            return "비식별 처리됨";
        }

        return member.getNickname();
    }

    private String trimText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "...";
    }

    private record TargetSnapshot(
            String targetNameSnapshot,
            Long writerMemberId,
            String writerNicknameSnapshot,
            Long parentPostId,
            String parentPostTitleSnapshot,
            String communityCode,
            String communityName
    ) {
    }

    private ReportCommunitySnapshot resolveReportCommunitySnapshot(
            ReportTargetType targetType,
            TargetSnapshot targetSnapshot,
            String requestCommunityCode
    ) {
        if (targetType != ReportTargetType.MEMBER) {
            return new ReportCommunitySnapshot(
                    targetSnapshot.communityCode(),
                    targetSnapshot.communityName()
            );
        }

        if (requestCommunityCode == null || requestCommunityCode.isBlank()) {
            return new ReportCommunitySnapshot(null, null);
        }

        String normalizedCode = requestCommunityCode.trim().toUpperCase();

        CountryCommunity community = countryCommunityRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 국가 커뮤니티입니다."));

        return new ReportCommunitySnapshot(
                community.getCode(),
                community.getName()
        );
    }

    private record ReportCommunitySnapshot(
            String communityCode,
            String communityName
    ) {
    }

}