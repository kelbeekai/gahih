package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.AdminPostResponse;
import com.gahih.domain.admin.dto.AdminPostSearchCondition;
import com.gahih.domain.admin.entity.AdminLog;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.repository.AdminLogRepository;
import com.gahih.domain.comment.repository.CommentReactionRepository;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.domain.post.repository.PostReactionRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.post.repository.PostTradeInfoRepository;
import com.gahih.domain.post.service.PostAttachmentService;
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
public class AdminPostService {

    private static final int MAX_PINNED_POST_COUNT = 5;

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final AdminLogRepository adminLogRepository;
    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostTradeInfoRepository postTradeInfoRepository;
    private final PostAttachmentService postAttachmentService;
    private final ReportResolutionService resolutionService;
    private final PostAttachmentRepository postAttachmentRepository;

    /**
     * 페이지네이션 적용 전 관리자 게시글 목록 메서드
     */
    public List<AdminPostResponse> findAllPosts() {
        return postRepository.findAllByOrderByIdDesc()
                .stream()
                .map(AdminPostResponse::from)
                .toList();
    }

    public Page<AdminPostResponse> searchPosts(AdminPostSearchCondition condition) {
        return postRepository.searchAdminPostPage(
                condition.getCommunityCode(),
                condition.getCategoryId(),
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachments(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSortName(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public List<AdminPostResponse> findPinnedPosts(
            String communityCode,
            AdminPostSearchCondition condition
    ) {
        int limit = condition.getCategoryId() == null ? Integer.MAX_VALUE : MAX_PINNED_POST_COUNT;

        return postRepository.findPinnedAdminPostList(
                communityCode,
                condition.getCategoryId(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                limit
        );
    }

    @Transactional
    public void deletePost(Long adminMemberId, Long postId) {
        Member admin = getAdmin(adminMemberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

/*      물리 삭제 로직
        commentRepository.deleteAllByPostId(postId);
        postAttachmentService.deleteAllByPost(post);
        postRepository.delete(post);
        saveLog(admin, "DELETE_POST", postId);
*/

        String beforeSnapshot = buildPostSnapshot(post);

        post.deleteByAdmin();

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.POST,
                post.getId(),
                admin.getId(),
                "관리자 게시글 삭제 조치"
        );

        String afterSnapshot = buildPostSnapshot(post);

//        saveLog(admin, "DELETE_POST", post.getId(), post.getTitle());

        saveLog(
                admin,
                "DELETE_POST",
                AdminLogTargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getCategory().getCountryCommunity().getCode(),
                post.getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot
        );
    }

    @Transactional
    public void blindPost(Long adminMemberId, Long postId) {
        Member admin = getAdmin(adminMemberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        String beforeSnapshot = buildPostSnapshot(post);

        post.blindByAdmin();

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.POST,
                post.getId(),
                admin.getId(),
                "관리자 게시글 블라인드 조치"
        );

        String afterSnapshot = buildPostSnapshot(post);

//        saveLog(admin, "BLIND_POST", post.getId(), post.getTitle());

        saveLog(
                admin,
                "BLIND_POST",
                AdminLogTargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getCategory().getCountryCommunity().getCode(),
                post.getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot
        );
    }

    /**
     * 관리자 게시글 복구 메서드
     */
    @Transactional
    public void restorePost(Long adminMemberId, Long postId) {
        Member admin = getAdmin(adminMemberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        String beforeSnapshot = buildPostSnapshot(post);

        post.restoreByAdmin();

        String afterSnapshot = buildPostSnapshot(post);

//        saveLog(admin, "RESTORE_POST", post.getId(), post.getTitle());

        saveLog(admin,
                "RESTORE_POST",
                AdminLogTargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getCategory().getCountryCommunity().getCode(),
                post.getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot);
    }

    /**
     * 관리자 게시글 영구 삭제
     */
    @Transactional
    public void hardDeletePost(Long adminMemberId, Long postId, String reason) {
        Member admin = getAdmin(adminMemberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        if (!post.isHardDeletableByAdmin()) {
            throw new BusinessException("관리자 삭제 처리된 게시글만 영구 삭제할 수 있습니다.");
        }

        String targetName = post.getTitle();

        String beforeSnapshot = buildPostSnapshot(post);

        String communityCode = post.getCategory().getCountryCommunity().getCode();
        String communityName = post.getCategory().getCountryCommunity().getName();

        commentReactionRepository.deleteAllByCommentPostId(postId);
        commentRepository.deleteAllByPostId(postId);
        postReactionRepository.deleteAllByPostId(postId);
        postTradeInfoRepository.deleteByPostId(postId);
        postAttachmentService.hardDeleteAllByPost(post);
        postRepository.delete(post);

        String afterSnapshot = "hardDeleted=true, postId=" + postId;

        String normalizedReason = requireReason(reason, "게시글 영구삭제 사유");

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.POST,
                postId,
                admin.getId(),
                normalizedReason
        );

//        saveLog(admin, "HARD_DELETE_POST", postId, targetName);

        saveLog(admin,
                "HARD_DELETE_POST",
                AdminLogTargetType.POST,
                postId,
                targetName,
                communityCode,
                communityName,
                normalizedReason,
                beforeSnapshot,
                afterSnapshot);
    }

    @Transactional
    public void pinPost(Long adminMemberId, Long postId) {
        Member admin = getAdmin(adminMemberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

//        postService.validatePinAllowed(post); // 특정 카테고리만 고정 기능이 가능한지 확인

        String communityCode = post.getCategory().getCountryCommunity().getCode();

        long pinnedCount = postRepository.findPinnedAdminPostList(
                communityCode,
                post.getCategory().getId(),
                null,
                null,
                MAX_PINNED_POST_COUNT
        ).size();

        if (!post.isPinned() && pinnedCount >= MAX_PINNED_POST_COUNT) {
            throw new BusinessException("고정글은 게시판별로 최대 5개까지 설정할 수 있습니다.");
        }

        String beforeSnapshot = buildPostSnapshot(post);

        post.pin();

        String afterSnapshot = buildPostSnapshot(post);

//        saveLog(admin, "PIN_POST", post.getId());
//        saveLog(admin, "PIN_POST", post.getId(), post.getTitle());

        saveLog(admin,
                "PIN_POST",
                AdminLogTargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getCategory().getCountryCommunity().getCode(),
                post.getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot);
    }

    @Transactional
    public void unpinPost(Long adminMemberId, Long postId) {
        Member admin = getAdmin(adminMemberId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        String beforeSnapshot = buildPostSnapshot(post);

        post.unpin();

        String afterSnapshot = buildPostSnapshot(post);

//        saveLog(admin, "UNPIN_POST", post.getId());
//        saveLog(admin, "UNPIN_POST", post.getId(), post.getTitle());

        saveLog(admin,
                "UNPIN_POST",
                AdminLogTargetType.POST,
                post.getId(),
                post.getTitle(),
                post.getCategory().getCountryCommunity().getCode(),
                post.getCategory().getCountryCommunity().getName(),
                null,
                beforeSnapshot,
                afterSnapshot);
    }

    /**
     * 관리자 첨부파일 단건 삭제 메서드
     */
    @Transactional
    public void deleteAttachment(Long adminMemberId, Long postId, Long attachmentId) {
        Member admin = getAdmin(adminMemberId);

        PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

        String targetName = attachment.getOriginalFileName();
        String beforeSnapshot = buildAttachmentSnapshot(attachment);

        postAttachmentService.deleteAttachmentByAdmin(postId, attachmentId);

        // 신고 조치 자동 연동
        resolutionService.resolveByActionIfPresent(
                ReportTargetType.ATTACHMENT,
                attachmentId,
                admin.getId(),
                "관리자 첨부파일 삭제 조치"
        );

        String afterSnapshot = buildAttachmentSnapshot(attachment);

//        saveLog(admin, "DELETE_ATTACHMENT", attachmentId);
//        saveLog(admin, "DELETE_ATTACHMENT", attachmentId, "첨부파일");

        saveLog(admin,
                "DELETE_ATTACHMENT",
                AdminLogTargetType.ATTACHMENT,
                attachmentId,
                "첨부파일",
                attachment.getPost().getCategory().getCountryCommunity().getCode(),
                attachment.getPost().getCategory().getCountryCommunity().getName(),
                null,
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

    private String buildPostSnapshot(Post post) {
        return "title=" + post.getTitle()
                + ", community=" + post.getCategory().getCountryCommunity().getName()
                + ", category=" + post.getCategory().getName()
                + ", status=" + post.getStatus().name()
                + ", pinned=" + post.isPinned()
                + ", secret=" + post.isSecret();
    }

    private String buildAttachmentSnapshot(PostAttachment attachment) {
        return "originalFileName=" + attachment.getOriginalFileName()
                + ", community=" + attachment.getPost().getCategory().getCountryCommunity().getName()
                + ", postId=" + attachment.getPost().getId()
                + ", status=" + attachment.getStatus().name()
                + ", downloadCount=" + attachment.getDownloadCount();
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
