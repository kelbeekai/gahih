package com.gahih.domain.admin.enumtype;

import lombok.Getter;

@Getter
public enum AdminMemberSearchType {
    ALL("전체"),
    USERNAME("아이디"),
    NICKNAME("닉네임"),
    EMAIL("이메일"),
    ROLE("권한"),
    STATUS("상태");

    private final String description;

    AdminMemberSearchType(String description) {
        this.description = description;
    }

}