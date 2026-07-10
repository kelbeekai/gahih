package com.gahih.domain.visit.service;

import com.gahih.domain.visit.dto.VisitorStatisticsSummary;
import com.gahih.domain.visit.entity.VisitorStatistics;
import com.gahih.domain.visit.repository.VisitorStatisticsRepository;
import com.gahih.global.policy.SessionCountPolicyService;
import com.gahih.global.policy.SessionCountPolicyType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitorStatisticsService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String VISITOR_COUNTED_DATE_COOKIE_NAME = "GAHIH_VISITOR_COUNTED_DATE";
    private static final Duration VISITOR_COUNTED_DATE_COOKIE_MAX_AGE = Duration.ofDays(400);

    private final VisitorStatisticsRepository visitorStatisticsRepository;
    private final SessionCountPolicyService sessionCountPolicyService;

    @Transactional
    public void increaseIfNeeded(HttpServletRequest request, HttpServletResponse response) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        if (hasAlreadyCountedToday(request, today)) {
            return;
        }

        long todayKey = today.toEpochDay();
        boolean shouldIncrease = sessionCountPolicyService.shouldIncreaseOncePerSession(
                request,
                SessionCountPolicyType.VISITOR_DAILY,
                todayKey
        );

        if (!shouldIncrease) {
            addCountedDateCookie(request, response, today);
            return;
        }

        VisitorStatistics statistics = visitorStatisticsRepository.findByVisitDate(today)
                .orElseGet(() -> visitorStatisticsRepository.save(VisitorStatistics.create(today)));

        statistics.increase();
        addCountedDateCookie(request, response, today);
    }

    public VisitorStatisticsSummary getSummary() {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        long todayCount = visitorStatisticsRepository.findByVisitDate(today)
                .map(VisitorStatistics::getDailyCount)
                .orElse(0L);

        long totalCount = visitorStatisticsRepository.sumTotalCount();

        return VisitorStatisticsSummary.of(todayCount, totalCount);
    }

    private boolean hasAlreadyCountedToday(HttpServletRequest request, LocalDate today) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return false;
        }

        String todayValue = today.toString();
        return Arrays.stream(cookies)
                .anyMatch(cookie -> VISITOR_COUNTED_DATE_COOKIE_NAME.equals(cookie.getName())
                        && todayValue.equals(cookie.getValue()));
    }

    private void addCountedDateCookie(HttpServletRequest request, HttpServletResponse response, LocalDate countedDate) {
        ResponseCookie cookie = ResponseCookie.from(VISITOR_COUNTED_DATE_COOKIE_NAME, countedDate.toString())
                .path("/")
                .maxAge(VISITOR_COUNTED_DATE_COOKIE_MAX_AGE)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        return request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
    }
}