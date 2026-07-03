package com.gahih.domain.post.dto;

import com.gahih.domain.post.entity.PostAttachment;
import lombok.Getter;

@Getter
public class PostAttachmentResponse {

    private final Long id;
    private final String originalFileName;
    private final Long fileSize;

    private PostAttachmentResponse(Long id, String originalFileName, Long fileSize) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
    }

    public static PostAttachmentResponse from(PostAttachment attachment) {
        return new PostAttachmentResponse(
                attachment.getId(),
                attachment.getOriginalFileName(),
                attachment.getFileSize()
        );
    }
}
