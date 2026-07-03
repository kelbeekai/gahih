package com.gahih.domain.post.dto;

import org.springframework.core.io.Resource;

public record PostAttachmentDownloadInfo(
        String originalFileName,
        Resource resource
) {
}
