package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.EmailAuthApiResponse;
import com.gahih.domain.member.dto.UsernameRecoverySendCodeRequest;
import com.gahih.domain.member.dto.UsernameRecoveryVerifyCodeRequest;
import com.gahih.domain.member.service.recovery.UsernameRecoveryFacade;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.DomainValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account-recovery/username")
public class UsernameRecoveryApiController {

    private final UsernameRecoveryFacade usernameRecoveryFacade;

    @PostMapping("/send-code")
    public EmailAuthApiResponse sendCode(
            @Valid @RequestBody UsernameRecoverySendCodeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return usernameRecoveryFacade.sendCode(request.getEmail());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public EmailAuthApiResponse verifyCode(
            @Valid @RequestBody UsernameRecoveryVerifyCodeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return EmailAuthApiResponse.fail(bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            return usernameRecoveryFacade.verifyCode(request.getEmail(), request.getCode());
        } catch (BusinessException | DomainValidationException e) {
            return EmailAuthApiResponse.fail(e.getMessage());
        }
    }
}