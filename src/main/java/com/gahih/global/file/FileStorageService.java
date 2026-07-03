package com.gahih.global.file;

import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_SINGLE_FILE_SIZE = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "pdf", "txt", "zip"
    );

    private static final Set<String> PREVIEWABLE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "pdf"
    );

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "sh", "bat", "cmd", "com", "scr", "msi",
            "jar", "war", "class", "js", "html", "htm", "php",
            "jsp", "jspx", "asp", "aspx", "svg"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    public StoredFileInfo store(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException("비어 있는 파일은 업로드할 수 없습니다.");
        }

        validateFileSize(multipartFile);

        String originalFileName = normalizeOriginalFileName(multipartFile.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        validateExtension(extension);

        String storedFileName = UUID.randomUUID() + "." + extension;
        Path uploadPath = getUploadPath();
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        validatePathInsideUploadDir(uploadPath, targetPath);

        try {
            Files.createDirectories(uploadPath);
            multipartFile.transferTo(targetPath);
        } catch (IOException e) {
            log.warn(
                    "File store failed. originalFileName={}, fileSize={}, contentType={}",
                    originalFileName,
                    multipartFile.getSize(),
                    multipartFile.getContentType(),
                    e
            );
            throw new BusinessException("파일 저장에 실패했습니다.");
        }

        return new StoredFileInfo(
                originalFileName,
                storedFileName,
                multipartFile.getContentType(),
                multipartFile.getSize()
        );
    }

    public Resource loadAsResource(String storedFileName) {
        Path uploadPath = getUploadPath();
        Path filePath = uploadPath.resolve(storedFileName).normalize();

        validatePathInsideUploadDir(uploadPath, filePath);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File resource not found or unreadable. storedFileName={}", storedFileName);
                throw new NotFoundException("존재하지 않는 첨부파일입니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            log.warn("Invalid file resource URL. storedFileName={}", storedFileName, e);
            throw new NotFoundException("존재하지 않는 첨부파일입니다.");
        }
    }

    public void delete(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            return;
        }

        Path uploadPath = getUploadPath();
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        validatePathInsideUploadDir(uploadPath, targetPath);

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            log.warn("File delete failed. storedFileName={}", storedFileName, e);
            throw new BusinessException("첨부파일 삭제에 실패했습니다.");
        }
    }

    public Path getStoredFilePath(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            throw new BusinessException("저장 파일명이 올바르지 않습니다.");
        }

        Path uploadPath = getUploadPath();
        Path filePath = uploadPath.resolve(storedFileName).normalize();

        validatePathInsideUploadDir(uploadPath, filePath);

        return filePath;
    }

    public boolean isPreviewable(String originalFileName) {
        String extension = extractExtension(originalFileName);
        return PREVIEWABLE_EXTENSIONS.contains(extension);
    }

    private void validateFileSize(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_SINGLE_FILE_SIZE) {
            throw new BusinessException("첨부파일 1개 용량은 10MB를 초과할 수 없습니다.");
        }
    }

    private String normalizeOriginalFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException("파일명이 올바르지 않습니다.");
        }

        String cleanedFileName = StringUtils.cleanPath(originalFileName);

        if (cleanedFileName.contains("..")
                || cleanedFileName.contains("/")
                || cleanedFileName.contains("\\")) {
            throw new BusinessException("파일명이 올바르지 않습니다.");
        }

        return cleanedFileName;
    }

    private String extractExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException("파일명이 올바르지 않습니다.");
        }

        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFileName.length() - 1) {
            throw new BusinessException("파일 확장자가 필요합니다.");
        }

        return originalFileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void validateExtension(String extension) {
        if (BLOCKED_EXTENSIONS.contains(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("허용되지 않는 파일 형식입니다.");
        }
    }

    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private void validatePathInsideUploadDir(Path uploadPath, Path targetPath) {
        if (!targetPath.startsWith(uploadPath)) {
            log.warn("Blocked file path traversal attempt.");
            throw new BusinessException("파일 경로가 올바르지 않습니다.");
        }
    }
}