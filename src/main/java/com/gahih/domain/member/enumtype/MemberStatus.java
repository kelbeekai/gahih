package com.gahih.domain.member.enumtype;

import lombok.Getter;

@Getter
public enum MemberStatus {
    ACTIVE("활동중"),
    SUSPENDED("정지"),
    WITHDRAWN("탈퇴 유예"),
    DELETED("최종 종료");

    private final String description;

    MemberStatus(String description) {
        this.description = description;
    }

}