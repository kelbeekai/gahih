package com.gahih.domain.post.controller;

import com.gahih.domain.admin.service.AdminPostService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostAttachmentDownloadInfo;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.post.service.PostAttachmentService;
import com.gahih.domain.post.service.PostAttachmentZipService;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts")
public class PostAttachmentController {

    private final PostAttachmentService postAttachmentService;
    private final PostAttachmentZipService postAttachmentZipService;
    private final AdminPostService adminPostService;
    private final PostRedirectPathBuilder postRedirectPathBuilder;

    @PostMapping("/{postId}/attachments/{attachmentId}/delete")
    public String deleteAttachment(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @PathVariable Long attachmentId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            HttpServletRequest request
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(
                    request,
                    postRedirectPathBuilder.editPath(communityCode, postId, fromCreate, detailContext)
            );
        }

        postAttachmentService.deleteAttachmentByOwner(loginMember.getId(), postId, attachmentId);
        return "redirect:" + postRedirectPathBuilder.editPath(communityCode, postId, fromCreate, detailContext);
    }

    /**
     * 관리자 첨부파일 삭제 엔드포인트
     */
    @PostMapping("/{postId}/attachments/{attachmentId}/admin-delete")
    public String adminDeleteAttachment(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @PathVariable Long attachmentId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            HttpServletRequest request
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(
                    request,
                    postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext)
            );
        }

        adminPostService.deleteAttachment(loginMember.getId(), postId, attachmentId);
        return "redirect:" + postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext);
    }

    @GetMapping("/{postId}/attachments/download-all")
    public ResponseEntity<byte[]> downloadAllAttachments(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            HttpServletRequest request
    ) {
        byte[] zipBytes = postAttachmentZipService.downloadAllAsZip(communityCode, postId, fromCreate, request);
        String zipFileName = postAttachmentZipService.createZipFileName(communityCode, postId);
        String encodedFileName = UriUtils.encode(zipFileName, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(zipBytes);
    }

    /**
     * 다운로드 카운트를 올리지 않는 프리뷰
     */
    @GetMapping("/attachments/{attachmentId}/preview")
    public ResponseEntity<Resource> previewAttachment(
            @PathVariable String communityCode,
            @PathVariable Long attachmentId
    ) {
        PostAttachmentDownloadInfo downloadInfo =
                postAttachmentService.getAttachmentResource(communityCode, attachmentId);
        String encodedFileName = UriUtils.encode(downloadInfo.originalFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName)
                .body(downloadInfo.resource());
    }

    /**
     * 다운로드 엔드포인트
     */
    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String communityCode,
            @PathVariable Long attachmentId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            HttpServletRequest request
    ) {
        PostAttachmentDownloadInfo downloadInfo =
                postAttachmentService.downloadAttachment(communityCode, attachmentId, fromCreate, request);
        String encodedFileName = UriUtils.encode(downloadInfo.originalFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(downloadInfo.resource());
    }
}