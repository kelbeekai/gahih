package com.gahih.domain.post.entity;

import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAttachment {

    private static final int FILE_NAME_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false, length = FILE_NAME_MAX_LENGTH)
    private String originalFileName;

    @Column(nullable = false, unique = true, length = FILE_NAME_MAX_LENGTH)
    private String storedFileName;

    @Column(length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    private LocalDateTime createdAt;

    private PostAttachment(Post post, String originalFileName, String storedFileName, String contentType, Long fileSize) {
        validatePost(post);
        validateOriginalFileName(originalFileName);
        validateStoredFileName(storedFileName);
        validateFileSize(fileSize);

        this.post = post;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static PostAttachment create(Post post, String originalFileName, String storedFileName, String contentType, Long fileSize) {
        return new PostAttachment(post, originalFileName, storedFileName, contentType, fileSize);
    }

    void changePost(Post post) {
        this.post = post;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new DomainValidationException("첨부파일이 속한 게시글은 필수입니다.");
        }
    }

    private void validateOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new DomainValidationException("원본 파일명은 비어 있을 수 없습니다.");
        }
        if (originalFileName.length() > FILE_NAME_MAX_LENGTH) {
            throw new DomainValidationException("원본 파일명은 255자를 초과할 수 없습니다.");
        }
    }

    private void validateStoredFileName(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new DomainValidationException("저장 파일명은 비어 있을 수 없습니다.");
        }
        if (storedFileName.length() > FILE_NAME_MAX_LENGTH) {
            throw new DomainValidationException("저장 파일명은 255자를 초과할 수 없습니다.");
        }
    }

    private void validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize < 0) {
            throw new DomainValidationException("파일 크기는 0 이상이어야 합니다.");
        }
    }
}
