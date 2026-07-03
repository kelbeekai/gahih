package com.gahih.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriUtils;

import java.net.URI;
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

        return createLoginPath(buildRequestPath(request));
    }

    public static String createLoginPathForPost(HttpServletRequest request, String fallbackPath) {
        if (request == null) {
            return createLoginPath(fallbackPath);
        }

        if (HttpMethod.GET.matches(request.getMethod())) {
            return createLoginPath(buildRequestPath(request));
        }

        String refererPath = extractInternalPathFromReferer(request);
        if (refererPath != null) {
            return createLoginPath(refererPath);
        }

        return createLoginPath(fallbackPath);
    }

    public static String createLoginPath(String redirectURL) {
        String safeRedirectURL = RedirectUrlValidator.validate(redirectURL);

        if ("/".equals(safeRedirectURL)) {
            return LOGIN_PATH;
        }

        String encodedRedirectURL = UriUtils.encodeQueryParam(safeRedirectURL, StandardCharsets.UTF_8);
        return LOGIN_PATH + "?" + REDIRECT_PARAM + "=" + encodedRedirectURL;
    }

    private static String buildRequestPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString == null ? requestURI : requestURI + "?" + queryString;
    }

    private static String extractInternalPathFromReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return null;
        }

        try {
            if (referer.startsWith("/")) {
                String validated = RedirectUrlValidator.validate(referer);
                return "/".equals(validated) ? null : validated;
            }

            URI refererUri = URI.create(referer);
            if (!isSameOrigin(request, refererUri)) {
                return null;
            }

            String path = refererUri.getRawPath();
            if (path == null || path.isBlank()) {
                return null;
            }

            String query = refererUri.getRawQuery();
            String combined = query == null ? path : path + "?" + query;
            String validated = RedirectUrlValidator.validate(combined);
            return "/".equals(validated) ? null : validated;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean isSameOrigin(HttpServletRequest request, URI refererUri) {
        String requestScheme = request.getScheme();
        String refererScheme = refererUri.getScheme();
        if (requestScheme == null || refererScheme == null || !requestScheme.equalsIgnoreCase(refererScheme)) {
            return false;
        }

        String requestHost = request.getServerName();
        String refererHost = refererUri.getHost();
        if (requestHost == null || refererHost == null || !requestHost.equalsIgnoreCase(refererHost)) {
            return false;
        }

        return resolvePort(requestScheme, request.getServerPort()) == resolvePort(refererScheme, refererUri.getPort());
    }

    private static int resolvePort(String scheme, int port) {
        if (port > 0) {
            return port;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return 80;
    }
}
