package com.gahih.domain.admin.controller;

import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class CommunityAdminHomeController {

    private final CategoryService categoryService;

    @GetMapping("/c/{communityCode}/admin")
    public String communityAdminHome(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));

        return "admin/community-home";
    }
}