package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.MemberPasswordResetRequest;
import com.gahih.domain.member.service.PasswordRecoveryFacade;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberRecoveryWebController {

    private final PasswordRecoveryFacade passwordRecoveryFacade;

    @GetMapping("/find-username")
    public String findUsernameForm() {
        return "members/member-find-username";
    }

    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "members/member-find-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(HttpSession session, Model model) {
        if (!passwordRecoveryFacade.hasValidPasswordResetSession(session)) {
            return "redirect:/members/find-password";
        }

        model.addAttribute("memberPasswordResetRequest", new MemberPasswordResetRequest());
        return "members/member-reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute MemberPasswordResetRequest memberPasswordResetRequest,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!passwordRecoveryFacade.hasValidPasswordResetSession(session)) {
            return "redirect:/members/find-password";
        }

        if (bindingResult.hasErrors()) {
            return "members/member-reset-password";
        }

        try {
            passwordRecoveryFacade.resetPassword(session, memberPasswordResetRequest);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해주세요."
            );
        } catch (BusinessException | DomainValidationException e) {
            bindingResult.reject("resetPasswordFail", e.getMessage());
            return "members/member-reset-password";
        }

        return "redirect:/members/login";
    }
}
