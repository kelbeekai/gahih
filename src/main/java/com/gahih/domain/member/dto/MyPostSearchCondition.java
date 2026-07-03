package com.gahih.domain.member.dto;

import com.gahih.domain.member.enumtype.MyPostSortType;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPostSearchCondition {

    private static final int DEFAULT_SIZE = 20;

    private String communityCode;
    private Long categoryId;
    private String keyword;
    private Boolean secret;
    private Boolean onlyWithAttachments;
    private TradeType tradeType;
    private TradeStatus tradeStatus;
    private MyPostSortType sort = MyPostSortType.LATEST;
    private int page = 1;
    private int size = DEFAULT_SIZE;

    public String getKeywordOrNull() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    public Boolean getSecretOrNull() {
        return secret;
    }

    public Boolean getOnlyWithAttachmentsOrNull() {
        return onlyWithAttachments;
    }

    public int getSafePage() {
        return Math.max(page, 1);
    }

    public int getSafeSize() {
        if (size == 40 || size == 60) {
            return size;
        }
        return DEFAULT_SIZE;
    }
}
