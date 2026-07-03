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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostAttachmentService {

    private static final int MAX_ATTACHMENT_COUNT = 3;

    private final PostAttachmentRepository postAttachmentRepository;
    private final FileStorageService fileStorageService;

    public List<PostAttachmentResponse> findAttachmentResponses(Long postId) {
        return postAttachmentRepository.findAllByPostIdOrderByIdAsc(postId)
                .stream()
                .map(PostAttachmentResponse::from)
                .toList();
    }

    public PostAttachmentDownloadInfo getDownloadAttachment(Long attachmentId) {
        PostAttachment attachment = postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 첨부파일입니다."));

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

        validateAttachmentCount(post.getAttachments().size() + validFiles.size());

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

        deleteAttachment(attachment.getPost(), attachment);
    }

    @Transactional
    public void deleteAllByPost(Post post) {
        List<PostAttachment> attachments = postAttachmentRepository.findAllByPostIdOrderByIdAsc(post.getId());

        for (PostAttachment attachment : attachments) {
            deleteAttachment(post, attachment);
        }
    }

    private void deleteAttachment(Post post, PostAttachment attachment) {
        fileStorageService.delete(attachment.getStoredFileName());
        post.removeAttachment(attachment);
        postAttachmentRepository.delete(attachment);
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
}
