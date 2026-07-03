package com.gahih.domain.admin.controller;

import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final CountryCommunityService countryCommunityService;

    @GetMapping
    public String adminHome(@Login LoginMember loginMember, Model model) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("communities", countryCommunityService.findEnabledCommunities());
        return "admin/admin-home";
    }

}