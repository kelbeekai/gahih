package com.gahih.domain.member.controller;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.member.dto.MemberMyPageResponse;
import com.gahih.domain.member.dto.MyPostSearchCondition;
import com.gahih.domain.member.service.account.MemberAccountService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MyPostController {

    private final MemberAccountService memberAccountService;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @GetMapping("/c/{communityCode}/mypage/posts")
    public String listMyPosts(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @ModelAttribute("condition") MyPostSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(communityCode);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));

        MemberMyPageResponse member = memberAccountService.getMyPage(loginMember.getId());

        Long effectiveCategoryId = condition.getCategoryId();

        if (member.isSuspended()) {
            Category inquiryCategory = categoryRepository
                    .findByCountryCommunity_CodeAndCode(
                            communityCode.toUpperCase(),
                            CategoryCode.INQUIRY
                    )
                    .orElseThrow(() -> new BusinessException("이용문의 게시판을 찾을 수 없습니다."));

            effectiveCategoryId = inquiryCategory.getId();
            condition.setCategoryId(effectiveCategoryId);
        }

        Category selectedCategory = categoryService.findByIdOrNull(effectiveCategoryId);

        boolean inquiryCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.INQUIRY);
        boolean marketCategory = selectedCategory != null && selectedCategory.isCode(CategoryCode.MARKET);

        if (!inquiryCategory) {
            condition.setSecret(null);
        }

        if (!marketCategory) {
            condition.setTradeType(null);
            condition.setTradeStatus(null);
        }

        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("inquiryCategory", inquiryCategory);
        model.addAttribute("marketCategory", marketCategory);
        model.addAttribute("tradeTypes", TradeType.values());
        model.addAttribute("tradeStatuses", TradeStatus.values());
        model.addAttribute("member", member);
        model.addAttribute("categories",
                member.isSuspended()
                        ? categoryService.findAllCategories(communityCode).stream()
                        .filter(category -> category.isCode(CategoryCode.INQUIRY))
                        .toList()
                        : categoryService.findAllCategories(communityCode)
        );
        model.addAttribute("postPage", postRepository.searchMyPostPage(
                communityCode,
                loginMember.getId(),
                effectiveCategoryId,
                condition.getKeywordOrNull(),
                condition.getSecretOrNull(),
                condition.getOnlyWithAttachmentsOrNull(),
                condition.getTradeType(),
                condition.getTradeStatus(),
                condition.getSort(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        ));

        return "members/mypage/member-post-list";
    }

    @GetMapping("/mypage/posts")
    public String oldMyPostsUrl() {
        return "redirect:/";
    }
}