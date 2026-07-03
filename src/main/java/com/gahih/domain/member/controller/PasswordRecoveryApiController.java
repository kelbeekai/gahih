package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.dto.PasswordRecoverySendCodeRequest;
import com.gahih.domain.member.dto.PasswordRecoveryVerifyCodeRequest;
import com.gahih.domain.member.service.recovery.PasswordRecoveryFacade;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account-recovery/password")
public class PasswordRecoveryApiController {

    private final PasswordRecoveryFacade passwordRecoveryFacade;

    @PostMapping("/send-code")
    public EmailAuthApiResponse sendCode(
            @Valid @RequestBody PasswordRecoverySendCodeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return passwordRecoveryFacade.sendCode(request.getUsername(), request.getEmail());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public EmailAuthApiResponse verifyCode(
            @Valid @RequestBody PasswordRecoveryVerifyCodeRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest
    ) {
        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            HttpSession session = httpServletRequest.getSession();
            return passwordRecoveryFacade.verifyCode(
                    request.getUsername(),
                    request.getEmail(),
                    request.getCode(),
                    session
            );
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }
}