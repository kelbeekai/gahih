package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.dto.SignUpEmailSendCodeRequest;
import com.gahih.domain.member.dto.SignUpEmailVerifyCodeRequest;
import com.gahih.domain.member.service.auth.SignUpEmailAuthFacade;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email-auth/signup")
public class SignUpEmailAuthApiController {

    private final SignUpEmailAuthFacade signUpEmailAuthFacade;

    @PostMapping("/send-code")
    public EmailAuthApiResponse sendCode(
            @Valid @RequestBody SignUpEmailSendCodeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return signUpEmailAuthFacade.sendCode(request.getEmail());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public EmailAuthApiResponse verifyCode(
            @Valid @RequestBody SignUpEmailVerifyCodeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return signUpEmailAuthFacade.verifyCode(request.getEmail(), request.getCode());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }
}