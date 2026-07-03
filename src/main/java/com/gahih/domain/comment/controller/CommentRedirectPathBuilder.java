package com.gahih.domain.comment.controller;

import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.post.dto.PostDetailContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
class CommentRedirectPathBuilder {

    String detailPathWithCommentAnchor(
            String communityCode,
            Long postId,
            boolean fromCreate,
            PostDetailContext detailContext,
            CommentSearchCondition commentCondition
    ) {
        return detailPathWithCommentState(
                communityCode,
                postId,
                fromCreate,
                detailContext,
                commentCondition
        ) + "#comments";
    }

    private String detailPathWithCommentState(
            String communityCode,
            Long postId,
            boolean fromCreate,
            PostDetailContext detailContext,
            CommentSearchCondition commentCondition
    ) {
        StringBuilder sb = new StringBuilder("/c/")
                .append(communityCode)
                .append("/posts/")
                .append(postId);

        boolean first = true;

        if (fromCreate) {
            sb.append("?fromCreate=true");
            first = false;
        }

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

        if (commentCondition.getSort() != null) {
            sb.append("&commentSort=").append(commentCondition.getSort().name());
        }

        sb.append("&commentPage=").append(commentCondition.getSafePage());

        return sb.toString();
    }
}