package com.gahih.domain.post.dto;

import com.gahih.domain.post.enumtype.PostSortType;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchCondition {

    private static final int DEFAULT_SIZE = 20;

    private String communityCode;
    private Long categoryId;
    private String keyword;
    private Boolean secret;
    private Boolean onlyWithAttachments;
    private TradeType tradeType;
    private TradeStatus tradeStatus;
    private Boolean dimClosedTrade;
    private PostSortType sort = PostSortType.LATEST;
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

    public boolean isDimClosedTradeEnabled() {
        return dimClosedTrade == null || dimClosedTrade;
    }
}
