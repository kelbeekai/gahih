package com.gahih.domain.comment.service;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.comment.dto.CommentResponse;
import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.domain.report.service.ReportPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentListService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final CommentReactionService commentReactionService;
    private final CommentMentionService commentMentionService;
    private final ReportPermissionService reportPermissionService;

    /**
     * 비로그인/기본 댓글 조회
     */
    public List<CommentResponse> findCommentsByPostId(Long postId) {

        return commentRepository.findAllByPostIdOrderByIdAsc(postId)
                .stream()
                .filter(comment -> comment.getStatus() != CommentStatus.ADMIN_DELETED)
                .map(comment -> {
                    String renderedContent = comment.isActive()
                            ? commentMentionService.renderMentionContent(comment)
                            : comment.getStatusMessage();

                    return CommentResponse.from(
                            comment,
                            null,
                            false,
                            false,
                            comment.getMember().getRole() == MemberRole.ADMIN,
                            false,
                            renderedContent
                    );
                })
                .toList();
    }

    /**
     * 로그인 사용자용 댓글 조회
     */
    public List<CommentResponse> findCommentsByPostId(Long postId, Long loginMemberId) {

        boolean viewerAdmin = loginMemberId != null && memberRepository.findById(loginMemberId)
                .map(Member::isAdmin)
                .orElse(false);

        return commentRepository.findAllByPostIdOrderByIdAsc(postId)
                .stream()
                .filter(comment -> comment.getStatus() != CommentStatus.ADMIN_DELETED)
                .map(comment -> {
                    ReactionType myReactionType = commentReactionService.findMyReactionType(comment.getId(), loginMemberId);

                    boolean commentActive = comment.isActive();
                    boolean postActive = comment.getPost().isActive();

                    boolean canReportComment = postActive
                            && commentActive
                            && reportPermissionService.canReportComment(loginMemberId, comment.getId());

                    boolean canReportWriter = commentActive
                            && postActive
                            && reportPermissionService.canReportMember(loginMemberId, comment.getMember().getId());

                    boolean writerAdmin = comment.getMember().getRole() == MemberRole.ADMIN;

                    boolean mentionableWriter = isMentionableWriter(
                            comment.getMember(),
                            loginMemberId,
                            viewerAdmin,
                            comment.getPost(),
                            commentActive
                    );

                    String renderedContent = commentActive
                            ? commentMentionService.renderMentionContent(comment)
                            : comment.getStatusMessage();

                    return CommentResponse.from(
                            comment,
                            myReactionType,
                            canReportComment,
                            canReportWriter,
                            writerAdmin,
                            mentionableWriter,
                            renderedContent
                    );
                })
                .toList();
    }

    public Page<CommentResponse> searchCommentsByPostId(
            Long postId,
            Long loginMemberId,
            CommentSearchCondition condition
    ) {
        Page<Comment> page = commentRepository.searchCommentPage(
                postId,
                condition.getSort(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );

        boolean viewerAdmin = loginMemberId != null && memberRepository.findById(loginMemberId)
                .map(Member::isAdmin)
                .orElse(false);

        List<CommentResponse> content = page.getContent().stream()
                .map(comment -> {
                    ReactionType myReactionType = commentReactionService.findMyReactionType(comment.getId(), loginMemberId);

                    boolean commentActive = comment.isActive();
                    boolean postActive = comment.getPost().isActive();

                    boolean canReportComment = postActive
                            && commentActive
                            && reportPermissionService.canReportComment(loginMemberId, comment.getId());

                    boolean canReportWriter = commentActive
                            && postActive
                            && reportPermissionService.canReportMember(loginMemberId, comment.getMember().getId());

                    boolean writerAdmin = comment.getMember().getRole() == MemberRole.ADMIN;

                    boolean mentionableWriter = isMentionableWriter(
                            comment.getMember(),
                            loginMemberId,
                            viewerAdmin,
                            comment.getPost(),
                            commentActive
                    );

                    String renderedContent = commentActive
                            ? commentMentionService.renderMentionContent(comment)
                            : comment.getStatusMessage();

                    return CommentResponse.from(
                            comment,
                            myReactionType,
                            canReportComment,
                            canReportWriter,
                            writerAdmin,
                            mentionableWriter,
                            renderedContent
                    );
                })
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    /**
     * 비로그인: 무조건 false → 댓글 작성자 버튼 안 보임
     * 로그인 일반 회원: ACTIVE 타인만 버튼
     * 로그인 관리자: ACTIVE 타인 + 이용문의의 정지 회원 버튼
     */
    private boolean isMentionableWriter(Member targetMember, Long viewerId, boolean viewerAdmin, Post post, boolean commentActive) {
        if (!commentActive || targetMember == null || viewerId == null) {
            return false;
        }

        if (targetMember.getId().equals(viewerId)) {
            return false;
        }

        if (targetMember.isActive()) {
            return true;
        }

        return viewerAdmin
                && post.getCategory().isCode(CategoryCode.INQUIRY)
                && targetMember.isSuspended();
    }
}
