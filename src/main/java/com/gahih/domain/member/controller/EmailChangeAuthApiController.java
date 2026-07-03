package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.dto.EmailChangeSendCodeRequest;
import com.gahih.domain.member.dto.EmailChangeVerifyCodeRequest;
import com.gahih.domain.member.service.email.EmailChangeAuthFacade;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email-auth/change-email")
public class EmailChangeAuthApiController {

    private final EmailChangeAuthFacade emailChangeAuthFacade;

    @PostMapping("/send-code")
    public EmailAuthApiResponse sendCode(
            @Login LoginMember loginMember,
            @Valid @RequestBody EmailChangeSendCodeRequest request,
            BindingResult bindingResult
    ) {
        if (loginMember == null) {
            return EmailAuthApiResponse.fail("로그인이 필요합니다.");
        }

        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return emailChangeAuthFacade.sendCode(loginMember.getId(), request.getEmail());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public EmailAuthApiResponse verifyCode(
            @Login LoginMember loginMember,
            @Valid @RequestBody EmailChangeVerifyCodeRequest request,
            BindingResult bindingResult
    ) {
        if (loginMember == null) {
            return EmailAuthApiResponse.fail("로그인이 필요합니다.");
        }

        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return emailChangeAuthFacade.verifyCode(loginMember.getId(), request.getEmail(), request.getCode());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }
}