package com.gahih.global.file;

public record StoredFileInfo(
        String originalFileName,
        String storedFileName,
        String contentType,
        long fileSize
) {
}
