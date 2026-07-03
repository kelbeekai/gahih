package com.gahih.global.util;

public final class RedirectUrlValidator {

    private RedirectUrlValidator() {
    }

    public static String validate(String redirectURL) {
        if (redirectURL == null || redirectURL.isBlank()) {
            return "/";
        }

        String trimmed = redirectURL.trim();

        if (!trimmed.startsWith("/")) {
            return "/";
        }

        if (trimmed.startsWith("//")) {
            return "/";
        }

        if (trimmed.contains("\r") || trimmed.contains("\n")) {
            return "/";
        }

        return trimmed;
    }
}
