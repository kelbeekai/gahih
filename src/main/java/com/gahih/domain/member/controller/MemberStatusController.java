package com.gahih.domain.member.controller;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.member.service.MemberService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberStatusWebController {

    private final MemberService memberService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/withdrawn")
    public String withdrawnPage(@Login LoginMember loginMember, Model model) {
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("withdrawnInfo", memberService.getWithdrawnInfo(loginMember.getId()));
        model.addAttribute("serverNow", java.time.LocalDateTime.now());
        return "members/member-withdrawn";
    }

    @PostMapping("/restore")
    public String restoreMember(@Login LoginMember loginMember) {
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        memberService.restoreWithdrawnMember(loginMember.getId());
        return "redirect:/";
    }

    /**
     * SUSPENDED 상태 회원 전용 안내 화면
     */
    @GetMapping("/suspended")
    public String suspendedPage(@Login LoginMember loginMember, Model model) {
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("suspendedInfo", memberService.getSuspendedInfo(loginMember.getId()));

        model.addAttribute("inquiryCategories",
                categoryRepository.findAll().stream()
                        .filter(category -> category.isCode(CategoryCode.INQUIRY))
                        .sorted(java.util.Comparator.comparing(
                                category -> category.getCountryCommunity().getDisplayOrder()
                        ))
                        .toList()
        );
        model.addAttribute("serverNow", java.time.LocalDateTime.now());
        return "members/member-suspended";
    }

}
