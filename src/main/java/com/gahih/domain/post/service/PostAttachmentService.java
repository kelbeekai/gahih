package com.gahih.domain.post.service;

import com.gahih.domain.post.dto.PostAttachmentDownloadInfo;
import com.gahih.domain.post.dto.PostAttachmentResponse;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.ForbiddenException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.file.FileStorageService;
import com.gahih.global.file.StoredFileInfo;
import com.gahih.global.policy.RecentPostBypassPolicyService;
import com.gahih.global.policy.SessionCountPolicyService;
import com.gahih.global.policy.SessionCountPolicyType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostAttachmentService {

    private static final int MAX_ATTACHMENT_COUNT = 3;
    private static final long MAX_SINGLE_FILE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_TOTAL_FILE_SIZE = 30L * 1024 * 1024;
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final PostAttachmentRepository postAttachmentRepository;
    private final FileStorageService fileStorageService;
    private final SessionCountPolicyService sessionCountPolicyService;
    private final RecentPostBypassPolicyService recentPostBypassPolicyService;

    public List<PostAttachmentResponse> findAttachmentResponses(Long postId) {
        return postAttachmentRepository.findAllByPostIdOrderByIdAsc(postId)
                .stream()
                .map(attachment -> PostAttachmentResponse.from(attachment, false))
                .toList();
    }

    /**
     * 미리보기/inline 용, 다운로드 횟수 증가 X
     */
    public PostAttachmentDownloadInfo getAttachmentResource(String communityCode, Long attachmentId) {
        PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

        validateAttachmentInCommunity(attachment, communityCode);

        if (!attachment.isActive()) {
            throw new ForbiddenException(attachment.getStatusMessage());
        }

        if (!fileStorageService.isPreviewable(attachment.getOriginalFileName())) {
            throw new ForbiddenException("미리보기를 지원하지 않는 파일 형식입니다.");
        }

        return new PostAttachmentDownloadInfo(
                attachment.getOriginalFileName(),
                fileStorageService.loadAsResource(attachment.getStoredFileName())
        );
    }

    /**
     * 다운로드 횟수 증가 (같은 세션 내 반복 다운로드 억제)
     */
    @Transactional
    public PostAttachmentDownloadInfo downloadAttachment(
            String communityCode,
            Long attachmentId,
            boolean fromCreate,
            HttpServletRequest request
    ) {
        PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

        validateAttachmentInCommunity(attachment, communityCode);

        if (!attachment.isActive()) {
            throw new ForbiddenException(attachment.getStatusMessage());
        }

        if (!attachment.getPost().isActive()) {
            throw new ForbiddenException("삭제 또는 블라인드 처리된 게시글의 첨부파일은 다운로드할 수 없습니다.");
        }

        Long postId = attachment.getPost().getId();
        boolean bypassActive = recentPostBypassPolicyService.isBypassActive(request, postId);

        if (!(fromCreate && bypassActive)) {
            String todayKey = LocalDate.now(KOREA_ZONE_ID).toString(); // 추가

//            boolean shouldIncreaseDownloadCount = sessionCountPolicyService.shouldIncrease(
            boolean shouldIncreaseDownloadCount = sessionCountPolicyService.shouldIncreaseOncePerSessionPerDay(
                    request,
                    SessionCountPolicyType.ATTACHMENT_DOWNLOAD,
                    attachmentId,
                    todayKey // 추가
            );

            if (shouldIncreaseDownloadCount) {
                attachment.increaseDownloadCount();
            }
        }

        return new PostAttachmentDownloadInfo(
                attachment.getOriginalFileName(),
                fileStorageService.loadAsResource(attachment.getStoredFileName())
        );
    }

    @Transactional
    public void saveAttachments(Post post, List<MultipartFile> attachments) {
        List<MultipartFile> validFiles = filterValidFiles(attachments);
        if (validFiles.isEmpty()) {
            return;
        }

        long activeAttachmentCount = post.getAttachments().stream()
                .filter(PostAttachment::isActive)
                .count();

        long activeAttachmentTotalSize = post.getAttachments().stream()
                .filter(PostAttachment::isActive)
                .mapToLong(PostAttachment::getFileSize)
                .sum();

        validateAttachmentCount((int) activeAttachmentCount + validFiles.size());

        validateSingleFileSize(validFiles);
        validateTotalFileSize(activeAttachmentTotalSize, validFiles);

        for (MultipartFile file : validFiles) {
            StoredFileInfo storedFileInfo = fileStorageService.store(file);
            PostAttachment attachment = PostAttachment.create(
                    post,
                    storedFileInfo.originalFileName(),
                    storedFileInfo.storedFileName(),
                    storedFileInfo.contentType(),
                    storedFileInfo.fileSize()
            );
            post.addAttachment(attachment);
            postAttachmentRepository.save(attachment);
        }
    }

    @Transactional
    public void deleteAttachmentByOwner(Long loginMemberId, Long postId, Long attachmentId) {
        PostAttachment attachment = postAttachmentRepository.findByIdAndPostId(attachmentId, postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

        if (!attachment.getPost().getMember().getId().equals(loginMemberId)) {
            throw new ForbiddenException("본인이 작성한 게시글의 첨부파일만 삭제할 수 있습니다.");
        }


        if (!attachment.getPost().isEditableByUser()) {
            throw new ForbiddenException("삭제 또는 블라인드 처리된 게시글의 첨부파일은 삭제할 수 없습니다.");
        }

        if (!attachment.isActive()) {
            throw new ForbiddenException("이미 삭제 처리된 첨부파일입니다.");
        }

//        softDeleteAttachmentByUser(attachment); // 사용자의 첨부파일 소프트삭제
        deleteAttachment(attachment.getPost(), attachment); // 사용자의 첨부파일 물리 삭제
    }

    /**
     * 관리자 첨부파일 삭제용 메서드
     */
    @Transactional
    public void deleteAttachmentByAdmin(Long postId, Long attachmentId) {
        PostAttachment attachment = postAttachmentRepository.findByIdAndPostId(attachmentId, postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

//        deleteAttachment(attachment.getPost(), attachment);
        softDeleteAttachmentByAdmin(attachment);
    }


    @Transactional
    public void deleteAllByPost(Post post) {
        List<PostAttachment> attachments = postAttachmentRepository.findAllByPostIdOrderByIdAsc(post.getId());

        for (PostAttachment attachment : attachments) {
//            deleteAttachment(post, attachment);
            if (attachment.isActive()) {
                softDeleteAttachmentByAdmin(attachment);
            }
        }
    }

    /**
     * 게시글 영구삭제 시 첨부도 같이 물리 삭제 메서드
     */
    @Transactional
    public void hardDeleteAllByPost(Post post) {
        List<PostAttachment> attachments = postAttachmentRepository.findAllByPostIdOrderByIdAsc(post.getId());

        for (PostAttachment attachment : attachments) {
            deleteAttachment(post, attachment);
        }
    }

    /**
     * 첨부파일 물리 삭제 메서드
     */
    private void deleteAttachment(Post post, PostAttachment attachment) {
        fileStorageService.delete(attachment.getStoredFileName());
        post.removeAttachment(attachment);
        postAttachmentRepository.delete(attachment);
    }

    /**
     * 사용자의 첨부파일 소프트삭제 메서드
     */
    private void softDeleteAttachmentByUser(PostAttachment attachment) {
        if (attachment.isActive()) {
            fileStorageService.delete(attachment.getStoredFileName());
            attachment.deleteByUser();
        }
    }

    private void softDeleteAttachmentByAdmin(PostAttachment attachment) {
        if (attachment.isActive()) {
            fileStorageService.delete(attachment.getStoredFileName());
            attachment.deleteByAdmin();
        }
    }

    private List<MultipartFile> filterValidFiles(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        return attachments.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private void validateAttachmentCount(int attachmentCount) {
        if (attachmentCount > MAX_ATTACHMENT_COUNT) {
            throw new BusinessException("첨부파일은 최대 3개까지 업로드할 수 있습니다.");
        }
    }

    private void validateSingleFileSize(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_SINGLE_FILE_SIZE) {
                throw new BusinessException(
                        "첨부파일 1개당 최대 용량은 " + formatFileSize(MAX_SINGLE_FILE_SIZE) + "를 초과할 수 없습니다."
                );
            }
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void validateTotalFileSize(long existingTotalFileSize, List<MultipartFile> files) {
        long newFileSize = files.stream()
                .mapToLong(MultipartFile::getSize)
                .sum();

        if (existingTotalFileSize + newFileSize > MAX_TOTAL_FILE_SIZE) {
            throw new BusinessException("첨부파일 총 용량은 " + formatFileSize(MAX_TOTAL_FILE_SIZE) + "를 초과할 수 없습니다.");
        }
    }

    private void validateAttachmentInCommunity(PostAttachment attachment, String communityCode) {
        validatePostInCommunity(attachment.getPost(), communityCode);
    }

    private void validatePostInCommunity(Post post, String communityCode) {
        if (communityCode == null || communityCode.isBlank()) {
            throw new ForbiddenException("국가 커뮤니티 정보가 없습니다.");
        }

        if (!post.getCategory().getCountryCommunity().isCode(communityCode)) {
            throw new NotFoundException("존재하지 않는 첨부파일입니다.");
        }
    }
}
