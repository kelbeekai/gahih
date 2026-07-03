package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.dto.AdminCommentSearchCondition;
import com.gahih.domain.admin.enumtype.AdminCommentSearchType;
import com.gahih.domain.admin.enumtype.AdminCommentSortType;
import com.gahih.domain.admin.service.AdminCommentService;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.comment.enumtype.CommentStatus;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommentService adminCommentService;
    private final CategoryService categoryService;

    @GetMapping("/admin/comments")
    public String globalCommentList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminCommentSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(null);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("commentPage", adminCommentService.searchComments(condition));
        model.addAttribute("searchTypes", AdminCommentSearchType.values());
        model.addAttribute("sortTypes", AdminCommentSortType.values());
        model.addAttribute("statuses", CommentStatus.values());
        return "admin/comments/admin-comment-list";
    }

    @PostMapping("/admin/comments/{commentId}/blind-from-list")
    public String globalBlindCommentFromList(
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.blindComment(loginMember.getId(), commentId);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/admin/comments";
    }

    @PostMapping("/admin/comments/{commentId}/delete-from-list")
    public String globalDeleteCommentFromList(
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.deleteComment(loginMember.getId(), commentId);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/admin/comments";
    }

    @PostMapping("/admin/comments/{commentId}/restore-from-list")
    public String globalRestoreCommentFromList(
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.restoreComment(loginMember.getId(), commentId);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/admin/comments";
    }

    @PostMapping("/admin/comments/{commentId}/hard-delete-from-list")
    public String globalHardDeleteCommentFromList(
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.hardDeleteComment(loginMember.getId(), commentId, reason);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/admin/comments";
    }

    @GetMapping("/c/{communityCode}/admin/comments")
    public String commentList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminCommentSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(communityCode);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("commentPage", adminCommentService.searchComments(condition));
        model.addAttribute("searchTypes", AdminCommentSearchType.values());
        model.addAttribute("sortTypes", AdminCommentSortType.values());
        model.addAttribute("statuses", CommentStatus.values());

        return "admin/comments/admin-comment-list";
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/blind")
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
        adminCommentService.blindComment(loginMember.getId(), commentId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/restore")
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
        adminCommentService.restoreComment(loginMember.getId(), commentId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/delete")
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
        adminCommentService.deleteComment(loginMember.getId(), commentId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/hard-delete")
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
        adminCommentService.hardDeleteComment(loginMember.getId(), commentId, reason);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext, commentCondition(commentSort, commentPage));
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/blind-from-list")
    public String blindCommentFromList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.blindComment(loginMember.getId(), commentId);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/comments";
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/delete-from-list")
    public String deleteCommentFromList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.deleteComment(loginMember.getId(), commentId);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/comments";
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/restore-from-list")
    public String restoreCommentFromList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.restoreComment(loginMember.getId(), commentId);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/comments";
    }

    @PostMapping("/c/{communityCode}/admin/comments/{commentId}/hard-delete-from-list")
    public String hardDeleteCommentFromList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long commentId,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        adminCommentService.hardDeleteComment(loginMember.getId(), commentId, reason);
        addAdminCommentSearchRedirectAttributes(searchType, keyword, status, sort, size, page, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/comments";
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

    private void addAdminCommentSearchRedirectAttributes(
            String searchType,
            String keyword,
            String status,
            String sort,
            Integer size,
            Integer page,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("searchType", searchType);
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("size", size == null ? 20 : size);
        redirectAttributes.addAttribute("page", page == null || page < 1 ? 1 : page);
    }
}