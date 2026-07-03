package com.gahih.domain.post.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeType {

    GIVE("나눔해요", "나눔해요", "나눔완료"),
    SELL("판매해요", "판매해요", "판매완료"),
    WANTED("구매해요", "구매해요", "구매완료");

    private final String displayName;
    private final String openLabel;
    private final String closedLabel;

    public String getStatusLabel(TradeStatus status) {
        if (status == TradeStatus.CLOSED) {
            return closedLabel;
        }
        return openLabel;
    }
}