package com.gahih.domain.post.controller;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostAttachmentResponse;
import com.gahih.domain.post.dto.PostCreateRequest;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.post.dto.PostUpdateRequest;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.post.service.PostAttachmentService;
import com.gahih.domain.post.service.PostService;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.policy.RecentPostBypassPolicyService;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts")
public class PostFormController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final PostAttachmentService postAttachmentService;
    private final RecentPostBypassPolicyService recentPostBypassPolicyService;
    private final PostRedirectPathBuilder postRedirectPathBuilder;

    @GetMapping("/new")
    public String createForm(
            @PathVariable String communityCode,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            HttpServletRequest request,
            Model model
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, postRedirectPathBuilder.newPath(communityCode, detailContext));
        }

        if (detailContext.getCategoryId() == null) {
            return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
        }

        Category selectedCategory = categoryService.findByIdOrNull(detailContext.getCategoryId());
        if (selectedCategory == null) {
            return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
        }

        if (!selectedCategory.getCountryCommunity().isCode(communityCode)) {
            return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
        }

        if (selectedCategory.isAdminWriteOnly() && loginMember.getRole() != MemberRole.ADMIN) {
            return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
        }

        PostCreateRequest postCreateRequest = new PostCreateRequest();
        postCreateRequest.setCategoryId(selectedCategory.getId());

        boolean secretAllowed = selectedCategory.isSecretPostAllowed();

        addTradeFormAttributes(model, selectedCategory);

        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("postCreateRequest", postCreateRequest);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("fixedCategory", true);
        model.addAttribute("secretAllowed", secretAllowed);
        model.addAttribute("detailContext", detailContext);

        return "posts/post-create-form";
    }

    @PostMapping("/new")
    public String create(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "detailSource", required = false) com.gahih.domain.post.enumtype.PostDetailSource detailSource,
            @RequestParam(name = "detailReturnUrl", required = false) String detailReturnUrl,
            @RequestParam(name = "detailCategoryId", required = false) Long detailCategoryId,
            @RequestParam(name = "detailKeyword", required = false) String detailKeyword,
            @RequestParam(name = "detailSecret", required = false) Boolean detailSecret,
            @RequestParam(name = "detailSort", required = false) String detailSort,
            @RequestParam(name = "detailOnlyWithAttachments", required = false) Boolean detailOnlyWithAttachments,
            @RequestParam(name = "detailPage", defaultValue = "1") int detailPage,
            @RequestParam(name = "detailSize", defaultValue = "20") int detailSize,
            @RequestParam(name = "detailTradeType", required = false) TradeType detailTradeType,
            @RequestParam(name = "detailTradeStatus", required = false) TradeStatus detailTradeStatus,
            @RequestParam(name = "detailDimClosedTrade", required = false) Boolean detailDimClosedTrade,
            @Valid @ModelAttribute PostCreateRequest postCreateRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {
        if (detailSource != null) {
            detailContext.setSource(detailSource);
        }
        detailContext.setReturnUrl(detailReturnUrl);
        detailContext.setCategoryId(detailCategoryId);
        detailContext.setKeyword(detailKeyword);
        detailContext.setSecret(detailSecret);
        detailContext.setSort(detailSort);
        detailContext.setOnlyWithAttachments(detailOnlyWithAttachments);
        detailContext.setPage(detailPage);
        detailContext.setSize(detailSize);
        detailContext.setTradeType(detailTradeType);
        detailContext.setTradeStatus(detailTradeStatus);
        detailContext.setDimClosedTrade(detailDimClosedTrade);

        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, postRedirectPathBuilder.newPath(communityCode, detailContext));
        }

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("communityCode", communityCode);

        Category selectedCategory = categoryService.findByIdOrNull(postCreateRequest.getCategoryId());
        if (selectedCategory == null) {
            return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
        }

        if (!selectedCategory.getCountryCommunity().isCode(communityCode)) {
            return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
        }

        boolean secretAllowed = selectedCategory.isSecretPostAllowed();

        addTradeFormAttributes(model, selectedCategory);

        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("fixedCategory", true);
        model.addAttribute("secretAllowed", secretAllowed);
        model.addAttribute("detailContext", detailContext);

        if (bindingResult.hasErrors()) {
            return "posts/post-create-form";
        }

        try {
            Long postId = postService.createPost(communityCode, loginMember.getId(), postCreateRequest);

            recentPostBypassPolicyService.activateBypass(request, postId);

            return redirectToDetail(communityCode, postId, true, detailContext);
        } catch (BusinessException e) {
            bindingResult.reject("postCreateFailed", e.getMessage());
            return "posts/post-create-form";
        }

    }

    @GetMapping("/{postId}/edit")
    public String editForm(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            HttpServletRequest request,
            Model model
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, postRedirectPathBuilder.editPath(communityCode, postId, fromCreate, detailContext));
        }

        if (fromCreate) {
            recentPostBypassPolicyService.activateBypass(request, postId);
        }

        PostUpdateRequest postUpdateRequest = postService.getPostUpdateForm(
                communityCode,
                loginMember.getId(),
                postId
        );

        Category selectedCategory = categoryService.findByIdOrNull(postUpdateRequest.getCategoryId());
        boolean secretAllowed = selectedCategory != null && selectedCategory.isCode(CategoryCode.INQUIRY);

        addTradeFormAttributes(model, selectedCategory);

        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("communityCode", communityCode);
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("postUpdateRequest", postUpdateRequest);
        model.addAttribute("categories", categoryService.findWritableCategories(communityCode, loginMember));
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("fixedCategory", selectedCategory != null);
        model.addAttribute("secretAllowed", secretAllowed);
        model.addAttribute("postId", postId);
        model.addAttribute("fromCreate", fromCreate);
        model.addAttribute("detailContext", detailContext);

        List<PostAttachmentResponse> attachments = postAttachmentService.findAttachmentResponses(postId);

        long activeAttachmentCount = attachments.stream()
                .filter(attachment -> !attachment.isDeleted())
                .count();

        long activeAttachmentTotalSize = attachments.stream()
                .filter(attachment -> !attachment.isDeleted())
                .mapToLong(PostAttachmentResponse::getFileSize)
                .sum();

        model.addAttribute("attachments", attachments);
        model.addAttribute("activeAttachmentCount", activeAttachmentCount);
        model.addAttribute("activeAttachmentTotalSize", activeAttachmentTotalSize);

        return "posts/post-edit-form";
    }

    @PostMapping("/{postId}/edit")
    public String edit(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "detailReturnUrl", required = false) String detailReturnUrl,
            @RequestParam(name = "detailCategoryId", required = false) Long detailCategoryId,
            @RequestParam(name = "detailKeyword", required = false) String detailKeyword,
            @RequestParam(name = "detailSecret", required = false) Boolean detailSecret,
            @RequestParam(name = "detailSort", required = false) String detailSort,
            @RequestParam(name = "detailOnlyWithAttachments", required = false) Boolean detailOnlyWithAttachments,
            @RequestParam(name = "detailPage", defaultValue = "1") int detailPage,
            @RequestParam(name = "detailSize", defaultValue = "20") int detailSize,
            @RequestParam(name = "detailTradeType", required = false) TradeType detailTradeType,
            @RequestParam(name = "detailTradeStatus", required = false) TradeStatus detailTradeStatus,
            @RequestParam(name = "detailDimClosedTrade", required = false) Boolean detailDimClosedTrade,
            @Login LoginMember loginMember,
            @Valid @ModelAttribute PostUpdateRequest postUpdateRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {
        detailContext.setReturnUrl(detailReturnUrl);
        detailContext.setCategoryId(detailCategoryId);
        detailContext.setKeyword(detailKeyword);
        detailContext.setSecret(detailSecret);
        detailContext.setSort(detailSort);
        detailContext.setOnlyWithAttachments(detailOnlyWithAttachments);
        detailContext.setPage(detailPage);
        detailContext.setSize(detailSize);
        detailContext.setTradeType(detailTradeType);
        detailContext.setTradeStatus(detailTradeStatus);
        detailContext.setDimClosedTrade(detailDimClosedTrade);

        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, postRedirectPathBuilder.editPath(communityCode, postId, fromCreate, detailContext));
        }

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("communityCode", communityCode);

        Category selectedCategory = categoryService.findByIdOrNull(postUpdateRequest.getCategoryId());
        boolean secretAllowed = selectedCategory != null && selectedCategory.isCode(CategoryCode.INQUIRY);

        addTradeFormAttributes(model, selectedCategory);

        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("fixedCategory", selectedCategory != null);
        model.addAttribute("categories", categoryService.findWritableCategories(communityCode, loginMember));
        model.addAttribute("secretAllowed", secretAllowed);
        model.addAttribute("postId", postId);
        model.addAttribute("fromCreate", fromCreate);
        model.addAttribute("detailContext", detailContext);

        List<PostAttachmentResponse> attachments = postAttachmentService.findAttachmentResponses(postId);

        long activeAttachmentCount = attachments.stream()
                .filter(attachment -> !attachment.isDeleted())
                .count();

        long activeAttachmentTotalSize = attachments.stream()
                .filter(attachment -> !attachment.isDeleted())
                .mapToLong(PostAttachmentResponse::getFileSize)
                .sum();

        model.addAttribute("attachments", attachments);
        model.addAttribute("activeAttachmentCount", activeAttachmentCount);
        model.addAttribute("activeAttachmentTotalSize", activeAttachmentTotalSize);

        if (bindingResult.hasErrors()) {
            return "posts/post-edit-form";
        }

        try {
            postService.updatePost(communityCode, loginMember.getId(), postId, postUpdateRequest);
        } catch (BusinessException e) {
            bindingResult.reject("postUpdateFailed", e.getMessage());
            return "posts/post-edit-form";
        }

        // 수정 직후에도 작성 직후와 동일하게 bypass 활성화
        recentPostBypassPolicyService.activateBypass(request, postId);

        return redirectToDetail(communityCode, postId, true, detailContext);
    }

    @PostMapping("/{postId}/delete")
    public String delete(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            HttpServletRequest request
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext));
        }

        postService.deletePost(communityCode, loginMember.getId(), postId);
        return "redirect:" + postRedirectPathBuilder.listPath(communityCode, detailContext);
    }

    private void addTradeFormAttributes(Model model, Category selectedCategory) {
        boolean marketCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.MARKET);
        model.addAttribute("marketCategory", marketCategory);
        model.addAttribute("tradeTypes", TradeType.values());
    }

    private String redirectToDetail(String communityCode, Long postId, boolean fromCreate, PostDetailContext detailContext) {
        return "redirect:" + postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext);
    }
}
