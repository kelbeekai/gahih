package com.gahih.global.file;

import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "pdf", "txt", "zip");

    @Value("${file.upload-dir}")
    private String uploadDir;

    public StoredFileInfo store(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException("비어 있는 파일은 업로드할 수 없습니다.");
        }

        String originalFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        validateExtension(extension);

        String storedFileName = UUID.randomUUID() + "." + extension;
        Path uploadPath = getUploadPath();
        Path targetPath = uploadPath.resolve(storedFileName).normalize();

        try {
            Files.createDirectories(uploadPath);
            multipartFile.transferTo(targetPath);
        } catch (IOException e) {
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
        Path filePath = getUploadPath().resolve(storedFileName).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("존재하지 않는 첨부파일입니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new NotFoundException("존재하지 않는 첨부파일입니다.");
        }
    }

    public void delete(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            return;
        }

        try {
            Files.deleteIfExists(getUploadPath().resolve(storedFileName).normalize());
        } catch (IOException e) {
            throw new BusinessException("첨부파일 삭제에 실패했습니다.");
        }
    }

    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
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
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("허용되지 않는 파일 형식입니다.");
        }
    }
}
