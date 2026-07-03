package com.gahih.domain.post.controller;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostSearchCondition;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.post.service.PostListService;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts")
public class PostListController {

    private final PostListService postListService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(
            @PathVariable String communityCode,
            @ModelAttribute("condition") PostSearchCondition condition,
            @Login LoginMember loginMember,
            Model model
    ) {
        condition.setCommunityCode(communityCode);

        Category selectedCategory = categoryService.findByIdOrNull(condition.getCategoryId());
        if (selectedCategory != null && !selectedCategory.getCountryCommunity().isCode(communityCode)) {
            return "redirect:/c/" + communityCode + "/posts";
        }

        boolean marketCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.MARKET);
        boolean inquiryCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.INQUIRY);

        if (!marketCategory) {
            condition.setTradeType(null);
            condition.setTradeStatus(null);
        }

        if (!inquiryCategory) {
            condition.setSecret(null);
        }

        Long loginMemberId = loginMember != null ? loginMember.getId() : null;
        boolean isAdmin = loginMember != null && loginMember.getRole() == MemberRole.ADMIN;

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("categories", categoryService.findAllCategories(communityCode));
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("pinnedPosts", postListService.findPinnedPosts(condition, loginMemberId, isAdmin));
        model.addAttribute("postPage", postListService.searchPosts(condition, loginMemberId, isAdmin));
        model.addAttribute("marketCategory", marketCategory);
        model.addAttribute("inquiryCategory", inquiryCategory);
        model.addAttribute("tradeTypes", TradeType.values());
        model.addAttribute("tradeStatuses", TradeStatus.values());
        return "posts/post-list";
    }
}
