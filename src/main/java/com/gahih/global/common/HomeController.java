package com.gahih.global.common;

import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CountryCommunityService countryCommunityService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String gate(@Login LoginMember loginMember, Model model) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("communities", countryCommunityService.findEnabledCommunities());
        return "home";
    }

    @GetMapping("/c/{communityCode}")
    public String communityHome(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", countryCommunityService.findResponseByCode(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("boardCategories", categoryService.findHeaderCategories(communityCode));
        return "community/community-home";
    }
}