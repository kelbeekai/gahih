package com.gahih.domain.post.controller;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostSearchCondition;
import com.gahih.domain.post.service.PostService;
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
public class PostListWebController {

    private final PostService postService;
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

        Long loginMemberId = loginMember != null ? loginMember.getId() : null;
        boolean isAdmin = loginMember != null && loginMember.getRole() == MemberRole.ADMIN;

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("categories", categoryService.findAllCategories(communityCode));
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("pinnedPosts", postService.findPinnedPosts(communityCode, condition.getCategoryId(), loginMemberId, isAdmin));
        model.addAttribute("postPage", postService.searchPosts(condition, loginMemberId, isAdmin));
        return "posts/post-list";
    }
}
