package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.dto.AdminPostSearchCondition;
import com.gahih.domain.admin.service.AdminPostService;
import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
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
public class AdminPostController {

    private final AdminPostService adminPostService;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/admin/posts")
    public String globalPostList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(null);
        normalizeTradeAndSecretFilters(condition);
        addTradeAndSecretFilterAttributes(condition, model);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("categories",
                categoryRepository.findAllByOrderByCountryCommunity_DisplayOrderAscDisplayOrderAsc());
        model.addAttribute("postPage", adminPostService.searchPosts(condition));
        model.addAttribute("pinnedPosts", adminPostService.findPinnedPosts(null, condition));
        model.addAttribute("globalAdminPostMode", true);

        return "admin/posts/admin-post-list";
    }

    @PostMapping("/admin/posts/{postId}/pin")
    public String globalPinPost(
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean secret,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean onlyWithAttachments,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) TradeType tradeType,
            @RequestParam(required = false) TradeStatus tradeStatus,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.pinPost(loginMember.getId(), postId);
        addGlobalAdminPostSearchRedirectAttributes(
                categoryId, keyword, secret, sort, onlyWithAttachments, tradeType, tradeStatus, size, page, redirectAttributes
        );
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/{postId}/unpin")
    public String globalUnpinPost(
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean secret,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean onlyWithAttachments,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) TradeType tradeType,
            @RequestParam(required = false) TradeStatus tradeStatus,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.unpinPost(loginMember.getId(), postId);
        addGlobalAdminPostSearchRedirectAttributes(
                categoryId, keyword, secret, sort, onlyWithAttachments, tradeType, tradeStatus, size, page, redirectAttributes
        );
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/{postId}/blind")
    public String globalBlindPost(
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean secret,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean onlyWithAttachments,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) TradeType tradeType,
            @RequestParam(required = false) TradeStatus tradeStatus,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.blindPost(loginMember.getId(), postId);
        addGlobalAdminPostSearchRedirectAttributes(
                categoryId, keyword, secret, sort, onlyWithAttachments, tradeType, tradeStatus, size, page, redirectAttributes
        );
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/{postId}/restore")
    public String globalRestorePost(
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean secret,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean onlyWithAttachments,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) TradeType tradeType,
            @RequestParam(required = false) TradeStatus tradeStatus,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.restorePost(loginMember.getId(), postId);
        addGlobalAdminPostSearchRedirectAttributes(
                categoryId, keyword, secret, sort, onlyWithAttachments, tradeType, tradeStatus, size, page, redirectAttributes
        );
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/{postId}/delete")
    public String globalDeletePost(
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean secret,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean onlyWithAttachments,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) TradeType tradeType,
            @RequestParam(required = false) TradeStatus tradeStatus,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.deletePost(loginMember.getId(), postId);
        addGlobalAdminPostSearchRedirectAttributes(
                categoryId, keyword, secret, sort, onlyWithAttachments, tradeType, tradeStatus, size, page, redirectAttributes
        );
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/{postId}/hard-delete")
    public String globalHardDeletePost(
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean secret,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean onlyWithAttachments,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) TradeType tradeType,
            @RequestParam(required = false) TradeStatus tradeStatus,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.hardDeletePost(loginMember.getId(), postId, reason);
        addGlobalAdminPostSearchRedirectAttributes(
                categoryId, keyword, secret, sort, onlyWithAttachments, tradeType, tradeStatus, size, page, redirectAttributes
        );
        return "redirect:/admin/posts";
    }

    private void addGlobalAdminPostSearchRedirectAttributes(
            Long categoryId,
            String keyword,
            Boolean secret,
            String sort,
            Boolean onlyWithAttachments,
            TradeType tradeType,
            TradeStatus tradeStatus,
            Integer size,
            Integer page,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("categoryId", categoryId);
        redirectAttributes.addAttribute("keyword", (keyword == null || keyword.isBlank()) ? null : keyword.trim());
        redirectAttributes.addAttribute("secret", secret);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("onlyWithAttachments", onlyWithAttachments);
        redirectAttributes.addAttribute("tradeType", tradeType);
        redirectAttributes.addAttribute("tradeStatus", tradeStatus);
        redirectAttributes.addAttribute("size", size == null ? 20 : size);
        redirectAttributes.addAttribute("page", page == null || page < 1 ? 1 : page);
    }

    @GetMapping("/c/{communityCode}/admin/posts")
    public String postList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(communityCode);
        normalizeTradeAndSecretFilters(condition);
        addTradeAndSecretFilterAttributes(condition, model);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("categories", categoryService.findAllCategories(communityCode));
        model.addAttribute("postPage", adminPostService.searchPosts(condition));
        model.addAttribute("pinnedPosts", adminPostService.findPinnedPosts(communityCode, condition));

        return "admin/posts/admin-post-list";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/delete")
    public String deletePost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.deletePost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/blind")
    public String blindPost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.blindPost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/restore")
    public String restorePost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.restorePost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/hard-delete")
    public String hardDeletePost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.hardDeletePost(loginMember.getId(), postId, reason);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/pin")
    public String pinPost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.pinPost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/unpin")
    public String unpinPost(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @ModelAttribute("condition") AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        adminPostService.unpinPost(loginMember.getId(), postId);
        addAdminPostSearchRedirectAttributes(condition, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/posts";
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/blind-from-detail")
    public String blindPostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext
    ) {
        adminPostService.blindPost(loginMember.getId(), postId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/restore-from-detail")
    public String restorePostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext
    ) {
        adminPostService.restorePost(loginMember.getId(), postId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/delete-from-detail")
    public String deletePostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext
    ) {
        adminPostService.deletePost(loginMember.getId(), postId);
        return redirectToPostDetail(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/c/{communityCode}/admin/posts/{postId}/hard-delete-from-detail")
    public String hardDeletePostFromDetail(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(required = false) String reason
    ) {
        adminPostService.hardDeletePost(loginMember.getId(), postId, reason);
        return "redirect:" + listPath(communityCode, detailContext);
    }

    private void addAdminPostSearchRedirectAttributes(
            AdminPostSearchCondition condition,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("categoryId", condition.getCategoryId());
        redirectAttributes.addAttribute("keyword", condition.getKeywordOrNull());
        redirectAttributes.addAttribute("secret", condition.getSecret());
        redirectAttributes.addAttribute("sort", condition.getSort());
        redirectAttributes.addAttribute("onlyWithAttachments", condition.getOnlyWithAttachments());
        redirectAttributes.addAttribute("tradeType", condition.getTradeType());
        redirectAttributes.addAttribute("tradeStatus", condition.getTradeStatus());
        redirectAttributes.addAttribute("size", condition.getSafeSize());
        redirectAttributes.addAttribute("page", condition.getSafePage());
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

        if (detailContext.getTradeType() != null) {
            sb.append(first ? "?" : "&").append("tradeType=").append(detailContext.getTradeType().name());
            first = false;
        }

        if (detailContext.getTradeStatus() != null) {
            sb.append(first ? "?" : "&").append("tradeStatus=").append(detailContext.getTradeStatus().name());
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

    private void normalizeTradeAndSecretFilters(AdminPostSearchCondition condition) {
        Category selectedCategory = categoryService.findByIdOrNull(condition.getCategoryId());

        boolean inquiryCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.INQUIRY);
        boolean marketCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.MARKET);

        if (!inquiryCategory) {
            condition.setSecret(null);
        }

        if (!marketCategory) {
            condition.setTradeType(null);
            condition.setTradeStatus(null);
        }
    }

    private void addTradeAndSecretFilterAttributes(AdminPostSearchCondition condition, Model model) {
        Category selectedCategory = categoryService.findByIdOrNull(condition.getCategoryId());

        boolean inquiryCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.INQUIRY);
        boolean marketCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.MARKET);

        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("inquiryCategory", inquiryCategory);
        model.addAttribute("marketCategory", marketCategory);
        model.addAttribute("tradeTypes", TradeType.values());
        model.addAttribute("tradeStatuses", TradeStatus.values());
    }
}