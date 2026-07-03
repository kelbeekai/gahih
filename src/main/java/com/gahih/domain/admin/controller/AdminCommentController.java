package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.service.AdminService;
import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/admin/comments")
public class AdminCommentController {

    private final AdminService adminService;

    @PostMapping("/{commentId}/blind")
    public String blindComment(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage
    ) {
        adminService.blindComment(loginMember.getId(), commentId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/{commentId}/restore")
    public String restoreComment(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage
    ) {
        adminService.restoreComment(loginMember.getId(), commentId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage
    ) {
        adminService.deleteComment(loginMember.getId(), commentId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/{commentId}/hard-delete")
    public String hardDeleteComment(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @RequestParam(required = false) String reason,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage
    ) {
        adminService.hardDeleteComment(loginMember.getId(), commentId, reason);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    private CommentSearchCondition commentCondition(CommentSortType commentSort, int commentPage) {
        CommentSearchCondition condition = new CommentSearchCondition();
        if (commentSort != null) {
            condition.setSort(commentSort);
        }
        condition.setPage(commentPage);
        return condition;
    }

    private String redirectToPostDetail(
            String communityCode,
            Long postId,
            boolean fromCreate,
            PostDetailContext detailContext,
            CommentSearchCondition commentCondition
    ) {
        return "redirect:" + detailPath(communityCode, postId, fromCreate, detailContext, commentCondition) + "#comments";
    }

    private String detailPath(
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
            sb.append(first ? "?" : "&").append("fromCreate=true");
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
        first = false;

        sb.append(first ? "?" : "&").append("size=").append(detailContext.getSafeSize());

        if (commentCondition.getSort() != null) {
            sb.append("&commentSort=").append(commentCondition.getSort().name());
        }

        sb.append("&commentPage=").append(commentCondition.getSafePage());

        return sb.toString();
    }
}