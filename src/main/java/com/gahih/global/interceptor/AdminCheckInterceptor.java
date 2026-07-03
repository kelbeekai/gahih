package com.gahih.global.interceptor;

import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.common.SessionConst;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AdminCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null) {

            log.warn(
                    "Blocked admin request without session. method={}, uri={}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            response.sendRedirect(LoginRedirectHelper.createLoginPathForPost(request, resolveFallbackPath(request)));
            return false;
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {

            log.warn(
                    "Blocked admin request without login member. method={}, uri={}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            response.sendRedirect(LoginRedirectHelper.createLoginPathForPost(request, resolveFallbackPath(request)));
            return false;
        }

        if (loginMember.getRole() != MemberRole.ADMIN) {

            log.warn(
                    "Blocked non-admin access. memberId={}, role={}, method={}, uri={}",
                    loginMember.getId(),
                    loginMember.getRole(),
                    request.getMethod(),
                    request.getRequestURI()
            );

            response.sendRedirect("/");
            return false;
        }

        return true;
    }

    private String resolveFallbackPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullRequestPath = requestUri + (queryString != null && !queryString.isBlank() ? "?" + queryString : "");

        if (requestUri.matches("^/c/[^/]+/admin(?:/.*)?$")) {
            return fullRequestPath;
        }

        if (requestUri.startsWith("/admin/members")) {
            return "/admin/members";
        }
        if (requestUri.startsWith("/admin/posts")) {
            return "/admin/posts";
        }
        if (requestUri.startsWith("/admin/comments")) {
            return "/admin/comments";
        }
        if (requestUri.startsWith("/admin/reports")) {
            return "/admin/reports";
        }
        if (requestUri.startsWith("/admin/reporter-activities")) {
            return "/admin/reporter-activities";
        }
        if (requestUri.startsWith("/admin/logs")) {
            return "/admin/logs";
        }
        if (requestUri.startsWith("/admin/nickname-histories")) {
            return "/admin/nickname-histories";
        }
        if (requestUri.startsWith("/admin/nickname-reservations")) {
            return "/admin/nickname-reservations";
        }

        return "/admin";
    }
}
