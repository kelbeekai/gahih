package com.gahih.global.interceptor;

import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginSessionValidityInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    public LoginSessionValidityInterceptor(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginMember loginMember = session == null ? null : (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return true;
        }

        if (isPasswordVersionChanged(loginMember)) {
            session.invalidate();
            response.sendRedirect("/members/login");
            return false;
        }

        return true;
    }

    private boolean isPasswordVersionChanged(LoginMember loginMember) {
        return memberRepository.findById(loginMember.getId())
                .map(member -> {
                    int currentPasswordVersion = member.getPasswordVersion() == null ? 0 : member.getPasswordVersion();
                    int sessionPasswordVersion = loginMember.getPasswordVersionAtLogin() == null
                            ? 0
                            : loginMember.getPasswordVersionAtLogin();

                    return currentPasswordVersion != sessionPasswordVersion;
                })
                .orElse(true);
    }
}