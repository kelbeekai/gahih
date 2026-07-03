package com.gahih.global.util;

public final class RedirectUrlValidator {

    private RedirectUrlValidator() {
    }

    public static String sanitize(String redirectURL) {
        if (redirectURL == null || redirectURL.isBlank()) {
            return "/";
        }

        if (!redirectURL.startsWith("/")) {
            return "/";
        }

        if (redirectURL.startsWith("//")) {
            return "/";
        }

        return redirectURL;
    }
}
