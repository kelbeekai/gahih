package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.dto.AdminPostSearchCondition;
import com.gahih.domain.admin.service.AdminService;
import com.gahih.domain.category.service.CategoryService;
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
@RequestMapping("/c/{communityCode}/admin/posts")
public class AdminPostController {

    private final AdminService adminService;
    private final CategoryService categoryService;

    @GetMapping
    public String postList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(communityCode);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("categories", categoryService.findAllCategories(communityCode));
        model.addAttribute("postPage", adminService.searchPosts(condition));
        model.addAttribute("pinnedPosts", adminService.findPinnedPosts(communityCode, condition.getCategoryId()));

        return "admin/posts";
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminService.deletePost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/{postId}/blind")
    public String blindPost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminService.blindPost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/{postId}/restore")
    public String restorePost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminService.restorePost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/{postId}/hard-delete")
    public String hardDeletePost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        adminService.hardDeletePost(loginMember.getId(), postId, reason);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/{postId}/pin")
    public String pinPost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminService.pinPost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/{postId}/unpin")
    public String unpinPost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminService.unpinPost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/{postId}/blind-from-detail")
    public String blindPostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext
    ) {
        adminService.blindPost(loginMember.getId(), postId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/{postId}/restore-from-detail")
    public String restorePostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext
    ) {
        adminService.restorePost(loginMember.getId(), postId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/{postId}/delete-from-detail")
    public String deletePostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext
    ) {
        adminService.deletePost(loginMember.getId(), postId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/{postId}/hard-delete-from-detail")
    public String hardDeletePostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(required = false) String reason
    ) {
        adminService.hardDeletePost(loginMember.getId(), postId, reason);
        return "redirect:" + listPath(communityCode, detailContext);
    }

    private String redirectToPostDetail(
            String communityCode,
            Long postId,
            boolean fromCreate,
            PostDetailContext detailContext
    ) {
        return "redirect:" + detailPath(communityCode, postId, fromCreate, detailContext);
    }

    private String detailPath(String communityCode, Long postId, boolean fromCreate, PostDetailContext detailContext) {
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

        return sb.toString();
    }

    private String listPath(String communityCode, PostDetailContext detailContext) {
        StringBuilder sb = new StringBuilder("/c/")
                .append(communityCode)
                .append("/admin/posts");

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

        return sb.toString();
    }

    private void addAdminPostSearchRedirectAttributes(
            AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("categoryId", condition.getCategoryId());
        redirectAttributes.addAttribute("keyword", condition.getKeywordOrNull());
        redirectAttributes.addAttribute("secret", condition.getSecret());
        redirectAttributes.addAttribute("sort", condition.getSort());
        redirectAttributes.addAttribute("onlyWithAttachments", condition.isOnlyWithAttachments());
        redirectAttributes.addAttribute("size", condition.getSafeSize());
        redirectAttributes.addAttribute("page", condition.getSafePage());
    }
}