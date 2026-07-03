package com.gahih.global.interceptor;

import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.common.SessionConst;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        LoginMember loginMember = session == null ? null : (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember != null) {
            return true;
        }

        log.warn(
                "Blocked unauthenticated request. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI()
        );

        response.sendRedirect(resolveLoginPath(request));
        return false;
    }

    private String resolveLoginPath(HttpServletRequest request) {
        String fallbackPath = resolveFallbackPath(request);
        return LoginRedirectHelper.createLoginPathForPost(request, fallbackPath);
    }

    private String resolveFallbackPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();

        String fullRequestPath = requestUri + (queryString != null && !queryString.isBlank() ? "?" + queryString : "");

        if (requestUri.matches("^/c/[^/]+/posts/new$")) {
            return fullRequestPath;
        }

        if (requestUri.matches("^/c/[^/]+/posts/\\d+/edit$")) {
            return fullRequestPath;
        }

        if (requestUri.matches("^/c/[^/]+/posts/\\d+/delete$")) {
            return buildDetailPathFromMutation(request, requestUri.replaceFirst("/delete$", ""));
        }

        if (requestUri.matches("^/c/[^/]+/posts/\\d+/attachments/\\d+/delete$")) {
            return buildEditPathFromMutation(request, requestUri.replaceFirst("/attachments/\\d+/delete$", "/edit"));
        }

        if (requestUri.matches("^/c/[^/]+/posts/\\d+/attachments/\\d+/admin-delete$")) {
            return buildDetailPathFromMutation(request, requestUri.replaceFirst("/attachments/\\d+/admin-delete$", ""));
        }

        if (requestUri.matches("^/c/[^/]+/posts/\\d+/comments(?:/.*)?$")) {
            return buildDetailPathFromMutation(request, requestUri.replaceFirst("/comments(?:/.*)?$", ""));
        }

        if (requestUri.matches("^/c/[^/]+/mypage(?:/.*)?$")) {
            return fullRequestPath;
        }

        if (requestUri.matches("^/mypage(?:/.*)?$")) {
            return fullRequestPath;
        }

        return "/";
    }

    private String buildDetailPathFromMutation(HttpServletRequest request, String basePath) {
        String query = buildQueryString(
                request,
                "fromCreate",
                "source",
                "returnUrl",
                "categoryId",
                "keyword",
                "secret",
                "sort",
                "onlyWithAttachments",
                "page",
                "size",
                "commentSort",
                "commentPage"
        );

        return basePath + (query.isBlank() ? "" : "?" + query);
    }

    private String buildEditPathFromMutation(HttpServletRequest request, String basePath) {
        String query = buildQueryString(
                request,
                "fromCreate",
                "source",
                "returnUrl",
                "categoryId",
                "keyword",
                "secret",
                "sort",
                "onlyWithAttachments",
                "page",
                "size"
        );

        return basePath + (query.isBlank() ? "" : "?" + query);
    }

    private String buildQueryString(HttpServletRequest request, String... parameterNames) {
        StringBuilder sb = new StringBuilder();

        for (String parameterName : parameterNames) {
            String value = request.getParameter(parameterName);
            if (value == null || value.isBlank()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(parameterName)
                    .append("=")
                    .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
        }

        return sb.toString();
    }
}