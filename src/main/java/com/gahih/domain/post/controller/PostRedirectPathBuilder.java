package com.gahih.domain.post.controller;

import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.post.dto.PostDetailContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
class PostRedirectPathBuilder {

    String detailPath(String communityCode, Long postId, boolean fromCreate, PostDetailContext detailContext) {
        StringBuilder sb = new StringBuilder("/c/")
                .append(communityCode)
                .append("/posts/")
                .append(postId);

        boolean first = true;

        if (fromCreate) {
            sb.append(first ? "?" : "&").append("fromCreate=true");
            first = false;
        }

        first = appendDetailContextQuery(sb, first, detailContext);
        return sb.toString();
    }

    String newPath(String communityCode, PostDetailContext detailContext) {
        StringBuilder sb = new StringBuilder("/c/")
                .append(communityCode)
                .append("/posts/new");

        appendDetailContextQuery(sb, true, detailContext);
        return sb.toString();
    }

    String editPath(String communityCode, Long postId, boolean fromCreate, PostDetailContext detailContext) {
        StringBuilder sb = new StringBuilder("/c/")
                .append(communityCode)
                .append("/posts/")
                .append(postId)
                .append("/edit");

        boolean first = true;

        if (fromCreate) {
            sb.append(first ? "?" : "&").append("fromCreate=true");
            first = false;
        }

        appendDetailContextQuery(sb, first, detailContext);
        return sb.toString();
    }

    String listPath(String communityCode, PostDetailContext detailContext) {
        if (detailContext.getReturnUrlOrNull() != null
                && !detailContext.isPostListSource()
                && !detailContext.isMyPostsSource()
                && !detailContext.isAdminPostsSource()) {
            return detailContext.getReturnUrlOrNull();
        }

        StringBuilder sb = new StringBuilder();

        if (detailContext.isMyPostsSource()) {
            sb.append("/c/").append(communityCode).append("/mypage/posts");
        } else if (detailContext.isAdminPostsSource()) {
            sb.append("/c/").append(communityCode).append("/admin/posts");
        } else {
            sb.append("/c/").append(communityCode).append("/posts");
        }

        boolean first = true;

        if (detailContext.getCategoryId() != null) {
            sb.append(first ? "?" : "&").append("categoryId=").append(detailContext.getCategoryId());
            first = false;
        }

        if (detailContext.getKeywordOrNull() != null) {
            sb.append(first ? "?" : "&")
                    .append("keyword=")
                    .append(UriUtils.encode(detailContext.getKeywordOrNull(), StandardCharsets.UTF_8));
            first = false;
        }

        if (detailContext.getSecretOrNull() != null) {
            sb.append(first ? "?" : "&").append("secret=").append(detailContext.getSecretOrNull());
            first = false;
        }

        if (detailContext.getOnlyWithAttachments() != null) {
            sb.append(first ? "?" : "&").append("onlyWithAttachments=").append(detailContext.getOnlyWithAttachments());
            first = false;
        }

        if (detailContext.getTradeType() != null) {
            sb.append(first ? "?" : "&").append("tradeType=").append(detailContext.getTradeType().name());
            first = false;
        }

        if (detailContext.getTradeStatus() != null) {
            sb.append(first ? "?" : "&").append("tradeStatus=").append(detailContext.getTradeStatus().name());
            first = false;
        }

        if (detailContext.getDimClosedTrade() != null) {
            sb.append(first ? "?" : "&").append("dimClosedTrade=").append(detailContext.getDimClosedTrade());
            first = false;
        }

        if (detailContext.getSort() != null && !detailContext.getSort().isBlank()) {
            sb.append(first ? "?" : "&").append("sort=").append(detailContext.getSort());
            first = false;
        }

        if ((detailContext.isAdminPostsSource() || detailContext.isMyPostsSource())
                && detailContext.getOnlyWithAttachments() != null) {
            sb.append(first ? "?" : "&").append("onlyWithAttachments=").append(detailContext.getOnlyWithAttachments());
            first = false;
        }

        sb.append(first ? "?" : "&").append("page=").append(detailContext.getSafePage());
        sb.append("&size=").append(detailContext.getSafeSize());

        return sb.toString();
    }

    String detailPathWithCommentState(
            String communityCode,
            Long postId,
            boolean fromCreate,
            PostDetailContext detailContext,
            CommentSearchCondition commentCondition,
            String mention,
            String focus
    ) {
        StringBuilder sb = new StringBuilder(detailPath(communityCode, postId, fromCreate, detailContext));

        boolean hasQuery = sb.indexOf("?") >= 0;

        if (commentCondition.getSort() != null) {
            sb.append(hasQuery ? "&" : "?").append("commentSort=").append(commentCondition.getSort().name());
            hasQuery = true;
        }

        sb.append(hasQuery ? "&" : "?").append("commentPage=").append(commentCondition.getSafePage());

        if (mention != null && !mention.isBlank()) {
            sb.append("&mention=").append(UriUtils.encode(mention, StandardCharsets.UTF_8));
        }

        if (focus != null && !focus.isBlank()) {
            sb.append("&focus=").append(UriUtils.encode(focus, StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

    private boolean appendDetailContextQuery(StringBuilder sb, boolean first, PostDetailContext detailContext) {
        if (detailContext.getSource() != null) {
            sb.append(first ? "?" : "&").append("source=").append(detailContext.getSource().name());
            first = false;
        }

        if (detailContext.getReturnUrlOrNull() != null) {
            sb.append(first ? "?" : "&")
                    .append("returnUrl=")
                    .append(UriUtils.encode(detailContext.getReturnUrlOrNull(), StandardCharsets.UTF_8));
            first = false;
        }

        if (detailContext.getCategoryId() != null) {
            sb.append(first ? "?" : "&").append("categoryId=").append(detailContext.getCategoryId());
            first = false;
        }

        if (detailContext.getKeywordOrNull() != null) {
            sb.append(first ? "?" : "&")
                    .append("keyword=")
                    .append(UriUtils.encode(detailContext.getKeywordOrNull(), StandardCharsets.UTF_8));
            first = false;
        }

        if (detailContext.getSecretOrNull() != null) {
            sb.append(first ? "?" : "&").append("secret=").append(detailContext.getSecretOrNull());
            first = false;
        }

        if (detailContext.getTradeType() != null) {
            sb.append(first ? "?" : "&").append("tradeType=").append(detailContext.getTradeType().name());
            first = false;
        }

        if (detailContext.getTradeStatus() != null) {
            sb.append(first ? "?" : "&").append("tradeStatus=").append(detailContext.getTradeStatus().name());
            first = false;
        }

        if (detailContext.getDimClosedTrade() != null) {
            sb.append(first ? "?" : "&").append("dimClosedTrade=").append(detailContext.getDimClosedTrade());
            first = false;
        }

        if (detailContext.getSort() != null && !detailContext.getSort().isBlank()) {
            sb.append(first ? "?" : "&").append("sort=").append(detailContext.getSort());
            first = false;
        }

        if (detailContext.getOnlyWithAttachments() != null) {
            sb.append(first ? "?" : "&").append("onlyWithAttachments=").append(detailContext.getOnlyWithAttachments());
            first = false;
        }

        sb.append(first ? "?" : "&").append("page=").append(detailContext.getSafePage());
        sb.append("&size=").append(detailContext.getSafeSize());

        return false;
    }
}