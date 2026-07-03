package com.gahih.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

public final class LoginRedirectHelper {

    private static final String LOGIN_PATH = "/members/login";
    private static final String REDIRECT_PARAM = "redirectURL";

    private LoginRedirectHelper() {
    }

    public static String createLoginPath(HttpServletRequest request) {
        if (request == null) {
            return LOGIN_PATH;
        }

        if (!HttpMethod.GET.matches(request.getMethod())) {
            return LOGIN_PATH;
        }

        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String target = queryString == null ? requestURI : requestURI + "?" + queryString;

        return createLoginPath(target);
    }

    public static String createLoginPath(String redirectURL) {
        String safeRedirectURL = RedirectUrlValidator.validate(redirectURL);

        if ("/".equals(safeRedirectURL)) {
            return LOGIN_PATH;
        }

        String encodedRedirectURL = UriUtils.encodeQueryParam(safeRedirectURL, StandardCharsets.UTF_8);
        return LOGIN_PATH + "?" + REDIRECT_PARAM + "=" + encodedRedirectURL;
    }
}
