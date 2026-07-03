package com.gahih.domain.post.dto;

import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.enumtype.AttachmentStatus;
import lombok.Getter;

import java.util.Locale;

@Getter
public class PostAttachmentResponse {

    private final Long id;
    private final String originalFileName;
    private final Long fileSize;
    private final boolean image;
    private final String extension;
    private final Long downloadCount;
    private final AttachmentStatus status;

    private final boolean canReport;

    private PostAttachmentResponse(
            Long id,
            String originalFileName,
            Long fileSize,
            boolean image,
            String extension,
            Long downloadCount,
            AttachmentStatus status,
            boolean canReport
    ) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.image = image;
        this.extension = extension;
        this.downloadCount = downloadCount;
        this.status = status;
        this.canReport = canReport;
    }

    public static PostAttachmentResponse from(PostAttachment attachment, boolean canReport) {
        String extension = extractExtension(attachment.getOriginalFileName());

        String displayFileName = attachment.isActive()
                ? attachment.getOriginalFileName()
                : attachment.getStatusMessage();

        return new PostAttachmentResponse(
                attachment.getId(),
                displayFileName,
                attachment.getFileSize(),
                attachment.isActive() && isImageExtension(extension),
                extension,
                attachment.getDownloadCount(),
                attachment.getStatus(),
                canReport
        );
    }

    private static String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean isImageExtension(String extension) {
        return extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("png")
                || extension.equals("gif");
    }

    public boolean isDeleted() {
        return status != AttachmentStatus.ACTIVE;
    }

    public boolean isAdminDeleted() {
        return status == AttachmentStatus.ADMIN_DELETED;
    }
}