package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.MemberPasswordChangeRequest;
import com.gahih.domain.member.dto.MemberUpdateRequest;
import com.gahih.domain.member.dto.MemberWithdrawRequest;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.service.EmailChangeAuthFacade;
import com.gahih.domain.member.service.MemberService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.common.SessionConst;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageAccountWebController {

    private final MemberService memberService;
    private final EmailChangeAuthFacade emailChangeAuthFacade;
    private final PostRepository postRepository;

    @GetMapping("/mypage/edit")
    public String editMyPageForm(@Login LoginMember loginMember, Model model) {
        MemberUpdateRequest memberUpdateRequest = memberService.getMemberUpdateForm(loginMember.getId());

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("memberUpdateRequest", memberUpdateRequest);
        model.addAttribute("originalEmail", memberUpdateRequest.getEmail());
        model.addAttribute("emailChangeVerified", false);
        return "members/mypage/member-edit-form";
    }

    @PostMapping("/mypage/edit")
    public String editMyPage(
            @Login LoginMember loginMember,
            @Valid @ModelAttribute MemberUpdateRequest memberUpdateRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, "/mypage/edit");
        }

        String originalEmail = memberService.getMemberUpdateForm(loginMember.getId()).getEmail();

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("originalEmail", originalEmail);
        model.addAttribute("emailChangeVerified",
                emailChangeAuthFacade.isVerified(memberUpdateRequest.getEmail(), loginMember.getId()));

        if (bindingResult.hasErrors()) {
            return "members/mypage/member-edit-form";
        }

        try {
            memberService.updateMember(loginMember.getId(), memberUpdateRequest);
            return "redirect:/mypage";
        } catch (BusinessException | DomainValidationException e) {
            bindingResult.reject("editFail", e.getMessage());
            return "members/mypage/member-edit-form";
        }
    }

    @GetMapping("/mypage/password")
    public String changePasswordForm(@Login LoginMember loginMember, Model model) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("memberPasswordChangeRequest", new MemberPasswordChangeRequest());
        return "members/mypage/member-password-form";
    }

    @PostMapping("/mypage/password")
    public String changePassword(
            @Login LoginMember loginMember,
            @Valid @ModelAttribute MemberPasswordChangeRequest memberPasswordChangeRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, "/mypage/password");
        }

        model.addAttribute("loginMember", loginMember);

        if (bindingResult.hasErrors()) {
            return "members/mypage/member-password-form";
        }

        try {
            memberService.changePassword(loginMember.getId(), memberPasswordChangeRequest);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해주세요."
            );

            return "redirect:/members/login";
        } catch (BusinessException | DomainValidationException e) {
            bindingResult.reject("passwordFail", e.getMessage());
            return "members/mypage/member-password-form";
        }
    }

    @GetMapping("/mypage/withdraw")
    public String withdrawForm(@Login LoginMember loginMember, Model model) {
        model.addAttribute("loginMember", loginMember);

        if (!memberService.isWithdrawAllowed(loginMember.getId())) {
            return "redirect:/members/suspended";
        }

        model.addAttribute("memberWithdrawRequest", new MemberWithdrawRequest());
        return "members/mypage/member-withdraw-form";
    }

    @PostMapping("/mypage/withdraw")
    public String withdraw(
            @Login LoginMember loginMember,
            @Valid @ModelAttribute MemberWithdrawRequest memberWithdrawRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {
        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, "/mypage/withdraw");
        }

        model.addAttribute("loginMember", loginMember);

        if (bindingResult.hasErrors()) {
            return "members/mypage/member-withdraw-form";
        }

        try {
            unpinAllPinnedPostsOfMember(loginMember.getId());

            memberService.withdraw(loginMember.getId(), memberWithdrawRequest.getPassword());

            Member withdrawnMember = memberService.getMember(loginMember.getId());

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute(SessionConst.LOGIN_MEMBER, new LoginMember(withdrawnMember));
            }

            return "redirect:/members/withdrawn";
        } catch (BusinessException | DomainValidationException e) {
            bindingResult.reject("withdrawFail", e.getMessage());
            return "members/mypage/member-withdraw-form";
        }
    }

    private void unpinAllPinnedPostsOfMember(Long memberId) {
        List<Post> pinnedPosts = postRepository.findAllByMemberIdAndPinnedTrue(memberId);

        for (Post post : pinnedPosts) {
            post.unpin();
        }
    }
}