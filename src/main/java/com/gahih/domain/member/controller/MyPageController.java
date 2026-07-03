package com.gahih.domain.member.controller;

import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.dto.MemberMyPageResponse;
import com.gahih.domain.member.service.MemberService;
import com.gahih.domain.member.service.MyActivityStatisticsService;
import com.gahih.domain.member.service.MyMentionedCommentService;
import com.gahih.domain.member.service.MyRecentInteractionService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class MyPageWebController {

    private final MemberService memberService;
    private final MyActivityStatisticsService myActivityStatisticsService;
    private final MyRecentInteractionService myRecentInteractionService;
    private final MyMentionedCommentService myMentionedCommentService;
    private final CountryCommunityService countryCommunityService;
    private final CategoryService categoryService;

    @GetMapping("/mypage")
    public String myPage(@Login LoginMember loginMember, Model model) {
        MemberMyPageResponse member = memberService.getMyPage(loginMember.getId());

        model.addAttribute("communities", countryCommunityService.findEnabledCommunities());
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("member", member);
        model.addAttribute("activityStatistics", myActivityStatisticsService.getStatistics(loginMember.getId()));

        if (!member.isSuspended()) {
            model.addAttribute("recentInteractionPosts",
                    myRecentInteractionService.getRecentInteractionPosts(loginMember.getId()));
        }

        model.addAttribute("mentionedComments",
                myMentionedCommentService.getRecentMentionedComments(
                        loginMember.getId(),
                        member.isSuspended()
                ));

        return "members/mypage/member-mypage";
    }

    @GetMapping("/c/{communityCode}/mypage")
    public String communityMyPage(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            Model model
    ) {
        MemberMyPageResponse member = memberService.getMyPage(loginMember.getId());

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("member", member);
        model.addAttribute("activityStatistics",
                myActivityStatisticsService.getStatistics(communityCode, loginMember.getId()));

        if (!member.isSuspended()) {
            model.addAttribute("recentInteractionPosts",
                    myRecentInteractionService.getRecentInteractionPosts(communityCode, loginMember.getId()));
        }

        model.addAttribute("mentionedComments",
                myMentionedCommentService.getRecentMentionedComments(
                        communityCode,
                        loginMember.getId(),
                        member.isSuspended()
                ));

        return "members/mypage/member-mypage";
    }
}