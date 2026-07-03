package com.gahih.domain.post.dto;

import com.gahih.domain.admin.dto.AdminPostSearchCondition;
import com.gahih.domain.admin.enumtype.AdminPostSortType;
import com.gahih.domain.member.dto.MyPostSearchCondition;
import com.gahih.domain.member.enumtype.MyPostSortType;
import com.gahih.domain.post.enumtype.PostDetailSource;
import com.gahih.domain.post.enumtype.PostSortType;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDetailContext {

    private static final int DEFAULT_SIZE = 20;

    private PostDetailSource source = PostDetailSource.POST_LIST;

    private Long categoryId;
    private String keyword;
    private Boolean secret;
    private String sort;
    private Boolean onlyWithAttachments;
    private String returnUrl;

    private TradeType tradeType;
    private TradeStatus tradeStatus;
    private Boolean dimClosedTrade;

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

    public int getSafePage() {
        return Math.max(page, 1);
    }

    public int getSafeSize() {
        if (size == 40 || size == 60) {
            return size;
        }
        return DEFAULT_SIZE;
    }

    public boolean isPostListSource() {
        return source == PostDetailSource.POST_LIST;
    }

    public boolean isMyPostsSource() {
        return source == PostDetailSource.MY_POSTS;
    }

    public boolean isAdminPostsSource() {
        return source == PostDetailSource.ADMIN_POSTS;
    }

    public PostSearchCondition toPostSearchCondition() {
        PostSearchCondition condition = new PostSearchCondition();
        condition.setCategoryId(categoryId);
        condition.setKeyword(keyword);
        condition.setSecret(secret);
        condition.setOnlyWithAttachments(onlyWithAttachments);
        condition.setTradeType(tradeType);
        condition.setTradeStatus(tradeStatus);
        condition.setDimClosedTrade(dimClosedTrade);
        condition.setPage(getSafePage());
        condition.setSize(getSafeSize());

        if (sort != null && !sort.isBlank()) {
            try {
                condition.setSort(PostSortType.valueOf(sort));
            } catch (IllegalArgumentException ignored) {
                condition.setSort(PostSortType.LATEST);
            }
        }

        return condition;
    }

    public MyPostSearchCondition toMyPostSearchCondition() {
        MyPostSearchCondition condition = new MyPostSearchCondition();
//        condition.setCommunityCode(null);
        condition.setCategoryId(categoryId);
        condition.setKeyword(keyword);
        condition.setSecret(secret);
        condition.setOnlyWithAttachments(onlyWithAttachments);
        condition.setTradeType(tradeType);
        condition.setTradeStatus(tradeStatus);
        condition.setPage(getSafePage());
        condition.setSize(getSafeSize());

        if (sort != null && !sort.isBlank()) {
            try {
                condition.setSort(MyPostSortType.valueOf(sort));
            } catch (IllegalArgumentException ignored) {
                condition.setSort(MyPostSortType.LATEST);
            }
        }

        return condition;
    }

    public AdminPostSearchCondition toAdminPostSearchCondition() {
        AdminPostSearchCondition condition = new AdminPostSearchCondition();
        condition.setCategoryId(categoryId);
        condition.setKeyword(keyword);
        condition.setSecret(secret);
        condition.setOnlyWithAttachments(onlyWithAttachments);
        condition.setTradeType(tradeType);
        condition.setTradeStatus(tradeStatus);
        condition.setPage(getSafePage());
        condition.setSize(getSafeSize());

        if (sort != null && !sort.isBlank()) {
            try {
                condition.setSort(AdminPostSortType.valueOf(sort));
            } catch (IllegalArgumentException ignored) {
                condition.setSort(AdminPostSortType.LATEST);
            }
        }

        return condition;
    }

    public boolean isListLikeSource() {
        return isPostListSource() || isMyPostsSource() || isAdminPostsSource();
    }

    public String getReturnUrlOrNull() {
        if (returnUrl == null || returnUrl.isBlank()) {
            return null;
        }
        return returnUrl.trim();
    }

    public boolean hasReturnUrl() {
        return getReturnUrlOrNull() != null;
    }

    public boolean isAdminMemberDetailSource() {
        return source == PostDetailSource.ADMIN_MEMBER_DETAIL;
    }

    public boolean isAdminReportsSource() {
        return source == PostDetailSource.ADMIN_REPORTS;
    }

    public boolean isAdminCommentsSource() {
        return source == PostDetailSource.ADMIN_COMMENTS;
    }
}