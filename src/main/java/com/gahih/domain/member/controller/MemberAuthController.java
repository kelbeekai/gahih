package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.MemberLoginRequest;
import com.gahih.domain.member.dto.MemberSignUpRequest;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.service.MemberService;
import com.gahih.domain.member.service.SignUpEmailAuthFacade;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.common.SessionConst;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import com.gahih.global.util.RedirectUrlValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberAuthWebController {

    private final MemberService memberService;
    private final SignUpEmailAuthFacade signUpEmailAuthFacade;

    @GetMapping("/signup")
    public String signUpForm(Model model) {
        model.addAttribute("memberSignUpRequest", new MemberSignUpRequest());
        model.addAttribute("signupEmailVerified", false);
        return "members/member-signup";
    }

    @PostMapping("/signup")
    public String signUp(
            @Valid @ModelAttribute MemberSignUpRequest memberSignUpRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("signupEmailVerified",
                signUpEmailAuthFacade.isVerified(memberSignUpRequest.getEmail()));

        if (bindingResult.hasErrors()) {
            return "members/member-signup";
        }

        try {
            memberService.signUp(memberSignUpRequest);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "회원가입에 성공했습니다! 입력한 아이디와 비밀번호로 로그인해주세요."
            );
        } catch (BusinessException | DomainValidationException e) {
            bindingResult.reject("signupFail", e.getMessage());
            return "members/member-signup";
        }

        return "redirect:/members/login";
    }

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(name = "redirectURL", required = false) String redirectURL,
            Model model
    ) {
        model.addAttribute("memberLoginRequest", new MemberLoginRequest());
        model.addAttribute("redirectURL", RedirectUrlValidator.validate(redirectURL));
        return "members/member-login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute MemberLoginRequest memberLoginRequest,
            BindingResult bindingResult,
            @RequestParam(name = "redirectURL", required = false) String redirectURL,
            HttpServletRequest request,
            Model model
    ) {
        String safeRedirectURL = RedirectUrlValidator.validate(redirectURL);

        if (bindingResult.hasErrors()) {
            model.addAttribute("redirectURL", safeRedirectURL);
            return "members/member-login";
        }

        try {
            Member member = memberService.login(memberLoginRequest);

            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_MEMBER, new LoginMember(member));

            if (member.isWithdrawn()) {
                return "redirect:/members/withdrawn";
            }

            if (member.isSuspended()) {
                return "redirect:/members/suspended";
            }

            return "redirect:" + safeRedirectURL;
        } catch (BusinessException | DomainValidationException e) {
            bindingResult.reject("loginFail", e.getMessage());
            model.addAttribute("redirectURL", safeRedirectURL);
            return "members/member-login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
