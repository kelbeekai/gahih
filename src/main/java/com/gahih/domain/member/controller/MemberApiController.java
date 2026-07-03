package com.gahih.domain.member.controller;

import com.gahih.domain.member.dto.*;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.service.auth.MemberAuthService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberAuthService memberAuthService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberSignUpResponse signUp(@Valid @RequestBody MemberSignUpRequest request) {
        return memberAuthService.signUp(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public MemberSessionLoginResponse login(
            @Valid @RequestBody MemberLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Member member = memberAuthService.login(request);

        HttpSession session = httpServletRequest.getSession();
        LoginMember loginMember = new LoginMember(member);
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        return MemberSessionLoginResponse.from(loginMember);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public String logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "로그아웃이 완료되었습니다.";
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public LoginMemberResponse me(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);

        if (session == null) {
            return LoginMemberResponse.guest();
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return LoginMemberResponse.guest();
        }

        return LoginMemberResponse.from(loginMember);
    }
}