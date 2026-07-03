package com.gahih.global.interceptor;

import com.gahih.global.policy.RecentPostBypassPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RecentPostBypassInterceptor implements HandlerInterceptor {

    private static final Pattern POST_DETAIL_PATTERN = Pattern.compile("^/c/[^/]+/posts/\\d+$");

    private final RecentPostBypassPolicyService recentPostBypassPolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Set<Long> activePostIds = recentPostBypassPolicyService.getActivePostIds(request);

        if (activePostIds.isEmpty()) {
            return true;
        }

        if (shouldKeepBypass(request, activePostIds)) {
            return true;
        }

        if (shouldClearBypass(request, handler)) {
            recentPostBypassPolicyService.clearAll(request);
        }

        return true;
    }

    private boolean shouldKeepBypass(HttpServletRequest request, Set<Long> activePostIds) {
        String uri = request.getRequestURI();
        boolean fromCreate = Boolean.parseBoolean(request.getParameter("fromCreate"));

        // 작성 직후 상세 페이지 자체
        if (fromCreate) {
            for (Long postId : activePostIds) {
                if (uri.matches("^/c/[^/]+/posts/" + postId + "$")) {
                    return true;
                }

                if (uri.matches("^/c/[^/]+/posts/" + postId + "/attachments/download-all$")) {
                    return true;
                }
            }

            // 첨부 개별 다운로드
            if (uri.matches("^/c/[^/]+/posts/attachments/\\d+/download$")) {
                return true;
            }
        }

        // 첨부 미리보기는 상세 페이지 내부 부가 요청으로 간주
        if (uri.matches("^/c/[^/]+/posts/attachments/\\d+/preview$")) {
            return true;
        }

        return false;
    }

    private boolean shouldClearBypass(HttpServletRequest request, Object handler) {

        if (!isHtmlDocumentNavigation(request, handler)) {
            return false;
        }

        String uri = request.getRequestURI();

        // 정적 리소스 / 공통 예외
        if (isStaticOrIgnoredRequest(uri)) {
            return false;
        }

        // 첨부 관련 요청은 상세 내부 동작이므로 해제하지 않음
        if (isAttachmentInternalRequest(uri)) {
            return false;
        }

        // 그 외 주요 페이지 이동도 해제
        return true;

/*
        // 일반 상세 진입(/posts/{id})은 작성 직후 화면을 벗어나 다시 들어온 것으로 보고 해제
        if (isPostDetailPage(uri)) {
            return true;
        }
*/

    }

    private boolean isHtmlDocumentNavigation(HttpServletRequest request, Object handler) {

        // GET 화면 이동 요청에서만 해제
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        String secFetchDest = request.getHeader("Sec-Fetch-Dest");
        if (secFetchDest != null && !secFetchDest.isBlank() && !"document".equalsIgnoreCase(secFetchDest)) {
            return false;
        }

        String accept = request.getHeader("Accept");
        return accept == null || accept.contains("text/html");
    }

    private boolean isStaticOrIgnoredRequest(String uri) {
        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.equals("/favicon.ico")
                || uri.equals("/error")
                || uri.startsWith("/h2-console/");
    }

    private boolean isAttachmentInternalRequest(String uri) {
        return uri.matches("^/c/[^/]+/posts/attachments/\\d+/(preview|download)$")
                || uri.matches("^/c/[^/]+/posts/\\d+/attachments/download-all$");
    }

    private boolean isPostDetailPage(String uri) {
        return POST_DETAIL_PATTERN.matcher(uri).matches();
    }
}