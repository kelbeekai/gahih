package com.gahih.domain.post.service;

import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.ForbiddenException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.file.FileStorageService;
import com.gahih.global.policy.RecentPostBypassPolicyService;
import com.gahih.global.policy.SessionCountPolicyService;
import com.gahih.global.policy.SessionCountPolicyType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostAttachmentZipService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final FileStorageService fileStorageService;
    private final SessionCountPolicyService sessionCountPolicyService;
    private final RecentPostBypassPolicyService recentPostBypassPolicyService;

    @Transactional
    public byte[] downloadAllAsZip(
            String communityCode,
            Long postId,
            boolean fromCreate,
            HttpServletRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);

        if (!post.isActive()) {
            throw new ForbiddenException("삭제 또는 블라인드 처리된 게시글의 첨부파일 ZIP은 다운로드할 수 없습니다.");
        }

        List<PostAttachment> attachments = postAttachmentRepository.findAllByPostIdOrderByIdAsc(postId)
                .stream()
                .filter(PostAttachment::isActive)
                .toList();

        if (attachments.isEmpty()) {
            throw new BusinessException("첨부파일이 없는 게시글입니다.");
        }

        boolean bypassActive = recentPostBypassPolicyService.isBypassActive(request, postId);

        if (!(fromCreate && bypassActive)) {
            String todayKey = LocalDate.now(KOREA_ZONE_ID).toString(); // 추가

//            boolean shouldIncreaseZipDownloadCount = sessionCountPolicyService.shouldIncrease(
            boolean shouldIncreaseZipDownloadCount = sessionCountPolicyService.shouldIncreaseOncePerSessionPerDay(
                    request,
                    SessionCountPolicyType.POST_ZIP_DOWNLOAD,
                    postId,
                    todayKey // 추가
            );

            if (shouldIncreaseZipDownloadCount) {
                post.increaseZipDownloadCount();
            }
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            Set<String> usedEntryNames = new HashSet<>();

            for (PostAttachment attachment : attachments) {
                Path filePath = fileStorageService.getStoredFilePath(attachment.getStoredFileName());

                if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                    log.warn(
                            "Zip download failed because stored file is missing or unreadable. postId={}, attachmentId={}",
                            postId,
                            attachment.getId()
                    );
                    throw new NotFoundException("존재하지 않는 첨부파일이 포함되어 있습니다.");
                }

                String zipEntryName = createUniqueZipEntryName(attachment.getOriginalFileName(), usedEntryNames);
                zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));

                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    inputStream.transferTo(zipOutputStream);
                }

                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.warn("Zip file creation failed. postId={}, communityCode={}", postId, communityCode, e);
            throw new BusinessException("첨부파일 ZIP 생성에 실패했습니다.");
        }
    }

    public String createZipFileName(String communityCode, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);

        if (post.getAttachments().isEmpty()) {
            throw new BusinessException("첨부파일이 없는 게시글입니다.");
        }

        boolean hasActiveAttachments = post.getAttachments().stream().anyMatch(PostAttachment::isActive);
        if (!hasActiveAttachments) {
            throw new BusinessException("첨부파일이 없는 게시글입니다.");
        }

        String sanitizedTitle = sanitizeFileName(post.getTitle());
        return sanitizedTitle + "_attachments.zip";
    }

    private String createUniqueZipEntryName(String originalFileName, Set<String> usedEntryNames) {
        String safeName = sanitizeFileName(originalFileName);

        if (!usedEntryNames.contains(safeName)) {
            usedEntryNames.add(safeName);
            return safeName;
        }

        String baseName = safeName;
        String extension = "";

        int lastDotIndex = safeName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            baseName = safeName.substring(0, lastDotIndex);
            extension = safeName.substring(lastDotIndex);
        }

        int sequence = 2;
        while (true) {
            String candidate = baseName + " (" + sequence + ")" + extension;
            if (!usedEntryNames.contains(candidate)) {
                usedEntryNames.add(candidate);
                return candidate;
            }
            sequence++;
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachments";
        }

        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
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
