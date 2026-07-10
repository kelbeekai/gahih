package com.gahih.global.interceptor;

import com.gahih.domain.visit.service.VisitorStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor
public class VisitorCountInterceptor implements HandlerInterceptor {

    private static final Set<String> BOT_USER_AGENT_KEYWORDS = Set.of(
            "bot",
            "crawler",
            "spider",
            "slurp",
            "scan",
            "scanner",
            "l9scan",
            "leakix",
            "curl",
            "wget",
            "python-requests",
            "go-http-client",
            "libredtail-http",
            "claudebot",
            "gptbot"
    );

    private final VisitorStatisticsService visitorStatisticsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!shouldCount(request, handler)) {
            return true;
        }

        visitorStatisticsService.increaseIfNeeded(request, response);
        return true;
    }

    private boolean shouldCount(HttpServletRequest request, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        if (isIgnoredPath(request.getRequestURI())) {
            return false;
        }

        if (!isHtmlDocumentNavigation(request)) {
            return false;
        }

        return !isBotRequest(request);
    }

    private boolean isIgnoredPath(String uri) {
        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.equals("/favicon.ico")
                || uri.equals("/robots.txt")
                || uri.equals("/sitemap.xml")
                || uri.startsWith("/.well-known/")
                || uri.equals("/error")
                || uri.startsWith("/h2-console/")
                || uri.startsWith("/api/")
                || uri.equals("/admin")
                || uri.startsWith("/admin/")
                || uri.matches("^/c/[^/]+/admin(?:/.*)?$")
                || uri.matches("^/c/[^/]+/posts/attachments/\\d+/(preview|download)$")
                || uri.matches("^/c/[^/]+/posts/\\d+/attachments/download-all$");
    }

    private boolean isHtmlDocumentNavigation(HttpServletRequest request) {
        String secFetchDest = request.getHeader("Sec-Fetch-Dest");
        if (secFetchDest != null && !secFetchDest.isBlank() && !"document".equalsIgnoreCase(secFetchDest)) {
            return false;
        }

        String accept = request.getHeader("Accept");
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("text/html");
    }

    private boolean isBotRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return true;
        }

        String normalizedUserAgent = userAgent.toLowerCase(Locale.ROOT);
        return BOT_USER_AGENT_KEYWORDS.stream()
                .anyMatch(normalizedUserAgent::contains);
    }
}