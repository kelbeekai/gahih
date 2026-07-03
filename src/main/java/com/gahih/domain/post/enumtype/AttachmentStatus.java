package com.gahih.domain.post.enumtype;

public enum AttachmentStatus {
    ACTIVE,
    USER_DELETED, // 사용자 첨부파일 삭제를 이대로 물리 삭제로 확정 시 추후 이 이넘과 관련 메서드 정리
    ADMIN_DELETED
}