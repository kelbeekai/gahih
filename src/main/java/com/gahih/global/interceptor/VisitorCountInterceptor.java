package com.gahih.global.interceptor;

import com.gahih.domain.visit.service.VisitorStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class VisitorCountInterceptor implements HandlerInterceptor {

    private final VisitorStatisticsService visitorStatisticsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        visitorStatisticsService.increaseIfNeeded(request);
        return true;
    }
}