package com.gahih.domain.post.service;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.post.dto.PostAttachmentResponse;
import com.gahih.domain.post.dto.PostDetailResponse;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostTradeInfo;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.domain.report.service.ReportPermissionService;
import com.gahih.global.exception.ForbiddenException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.policy.RecentPostBypassPolicyService;
import com.gahih.global.policy.SessionCountPolicyService;
import com.gahih.global.policy.SessionCountPolicyType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDetailService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final SessionCountPolicyService sessionCountPolicyService;
    private final RecentPostBypassPolicyService recentPostBypassPolicyService;
    private final PostReactionService postReactionService;
    private final PostTradeService postTradeService;
    private final ReportPermissionService reportPermissionService;

    /**
     * 상세 DTO 조립용 메서드
     */
    @Transactional
    public PostDetailResponse findPostDetail(
            String communityCode,
            Long postId,
            Long loginMemberId,
            boolean isAdmin,
            boolean adminOriginalVisible,
            boolean fromCreate,
            HttpServletRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);

        boolean secretViewable = canViewSecretPost(post, loginMemberId, isAdmin);
        boolean owner = loginMemberId != null && post.getMember().getId().equals(loginMemberId);

        if (post.isAdminDeleted() && !(owner || isAdmin)) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }

        boolean contentAvailable = canAccessPostContent(post, owner, isAdmin);

//        if (post.isViewCountAllowed()) {
        boolean bypassActive = recentPostBypassPolicyService.isBypassActive(request, postId);

        if (!(fromCreate && bypassActive)) {
            // 작성 직후 상세 화면에서는 계속 보호
            if (bypassActive) {
                // 작성 직후 화면을 벗어났다가 일반 진입한 순간 보호 해제
                recentPostBypassPolicyService.deactivateBypass(request, postId);
            }

            String todayKey = LocalDate.now(KOREA_ZONE_ID).toString(); // 추가

//                boolean shouldIncreaseViewCount = sessionCountPolicyService.shouldIncrease(
            boolean shouldIncreaseViewCount = sessionCountPolicyService.shouldIncreaseOncePerSessionPerDay(
                    request,
                    SessionCountPolicyType.POST_VIEW,
                    postId,
                    todayKey // 추가
            );

            if (shouldIncreaseViewCount) {
                post.increaseViewCount();
            }
        }

//        }

        List<PostAttachmentResponse> attachments = secretViewable
                ? post.getAttachments().stream()
                .map(attachment -> PostAttachmentResponse.from(
                        attachment,
                        post.isActive()
                                && attachment.isActive()
                                && reportPermissionService.canReportAttachment(loginMemberId, attachment.getId())
                ))
                .toList()
                : List.of();


        ReactionType myReactionType = (secretViewable && contentAvailable)
                ? postReactionService.findMyReactionType(postId, loginMemberId)
                : null;

        PostTradeInfo tradeInfo = postTradeService.findByPostIdOrNull(postId);

        boolean canReportPost = secretViewable
                && post.isActive()
                && reportPermissionService.canReportPost(loginMemberId, postId);

        boolean canReportWriter = secretViewable
                && post.isActive()
                && reportPermissionService.canReportMember(loginMemberId, post.getMember().getId());

        boolean writerAdmin = post.getMember().getRole() == MemberRole.ADMIN;

        boolean mentionableWriter = secretViewable
                && contentAvailable
                && post.getCategory().isCommentAllowed()
                && isMentionableWriter(
                post.getMember(),
                loginMemberId,
                isAdmin,
                post
        );

        boolean attachmentSectionVisible = !post.isUserDeletedContext()
                && (isAdmin || (post.isActive() && contentAvailable));

        boolean effectiveAdminOriginalVisible = adminOriginalVisible && !post.isUserDeletedContext();

        return PostDetailResponse.of(
                post,
                attachments,
                myReactionType,
                tradeInfo,
                secretViewable,
                contentAvailable,
                effectiveAdminOriginalVisible,
                canReportPost,
                canReportWriter,
                writerAdmin,
                mentionableWriter,
                attachmentSectionVisible
        );
    }

    private boolean canViewSecretPost(Post post, Long loginMemberId, boolean isAdmin) {
        if (!post.isSecret()) {
            return true;
        }

        if (isAdmin) {
            return true;
        }

        return loginMemberId != null && post.getMember().getId().equals(loginMemberId);
    }

    private boolean canAccessPostContent(Post post, boolean owner, boolean isAdmin) {

        if (post.isActive()) {
            return true;
        }

        if (post.isUserDeleted()) {
            return false;
        }

        if (post.isAdminBlinded()) {
            return false;
        }

        return false;
    }

    /**
     * 비로그인: 게시글 작성자도 버튼 안 보임
     * 로그인 시에만 버튼 노출
     * self mention 여전히 불가
     * 관리자 예외 정책 유지
     */
    private boolean isMentionableWriter(Member targetMember, Long viewerId, boolean viewerAdmin, Post post) {
        if (targetMember == null || viewerId == null || post == null) {
            return false;
        }

        if (!canUseMentionInPost(post, viewerId, viewerAdmin)) {
            return false;
        }

        if (targetMember.getId().equals(viewerId)) {
            return false;
        }

        boolean inquiryPost = post.getCategory().isCode(CategoryCode.INQUIRY);

        if (targetMember.isAdmin()) {
            return inquiryPost && targetMember.isActive();
        }

        if (targetMember.isActive()) {
            return true;
        }

        return viewerAdmin
                && inquiryPost
                && targetMember.isSuspended();
    }

    private boolean canUseMentionInPost(Post post, Long viewerId, boolean viewerAdmin) {
        if (post == null || viewerId == null) {
            return false;
        }

        if (!post.getCategory().isCommentAllowed()) {
            return false;
        }

        if (!post.isSecret()) {
            return true;
        }

        if (viewerAdmin) {
            return true;
        }

        return post.getMember().getId().equals(viewerId);
    }

    private void validatePostInCommunity(Post post, String communityCode) {
        if (communityCode == null || communityCode.isBlank()) {
            throw new ForbiddenException("국가 커뮤니티 정보가 없습니다.");
        }

        if (!post.getCategory().getCountryCommunity().isCode(communityCode)) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }
    }
}
