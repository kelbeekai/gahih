package com.gahih.domain.post.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {

    OPEN("진행중"),
    CLOSED("완료");

    private final String displayName;
}