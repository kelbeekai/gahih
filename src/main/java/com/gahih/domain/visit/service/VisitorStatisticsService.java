package com.gahih.domain.visit.service;

import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.visit.dto.VisitorStatisticsSummary;
import com.gahih.domain.visit.entity.MemberDailyVisit;
import com.gahih.domain.visit.entity.VisitorStatistics;
import com.gahih.domain.visit.enumtype.VisitorCountMode;
import com.gahih.domain.visit.repository.MemberDailyVisitRepository;
import com.gahih.domain.visit.repository.VisitorStatisticsRepository;
import com.gahih.global.common.SessionConst;
import com.gahih.global.policy.SessionCountPolicyService;
import com.gahih.global.policy.SessionCountPolicyType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final MemberDailyVisitRepository memberDailyVisitRepository;
    private final MemberRepository memberRepository;
    private final SessionCountPolicyService sessionCountPolicyService;

    @Value("${app.visit.count-mode:LOGIN_MEMBER_DAILY}")
    private VisitorCountMode visitorCountMode;

    // application.properties 파일 app.visit.count-mode=PUBLIC_COOKIE 설정 시 1차 개선 (공개 쿠키 기반 방문자 수 집계 방식)으로 되돌림
    @Transactional
    public void increaseIfNeeded(HttpServletRequest request, HttpServletResponse response) {
        if (visitorCountMode == VisitorCountMode.PUBLIC_COOKIE) {
            increasePublicCookieIfNeeded(request, response);
            return;
        }

        increaseLoginMemberDailyIfNeeded(request);
    }

    public VisitorStatisticsSummary getSummary() {
        if (visitorCountMode == VisitorCountMode.PUBLIC_COOKIE) {
            return getPublicCookieSummary();
        }

        return getLoginMemberDailySummary();
    }

    private VisitorStatisticsSummary getPublicCookieSummary() {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        long todayCount = visitorStatisticsRepository.findByVisitDate(today)
                .map(VisitorStatistics::getDailyCount)
                .orElse(0L);

        long totalCount = visitorStatisticsRepository.sumTotalCount();

        return VisitorStatisticsSummary.of(todayCount, totalCount);
    }

    private VisitorStatisticsSummary getLoginMemberDailySummary() {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        long todayCount = memberDailyVisitRepository.countByVisitDate(today);
        long totalCount = memberDailyVisitRepository.count();

        return VisitorStatisticsSummary.of(todayCount, totalCount);
    }

    private void increaseLoginMemberDailyIfNeeded(HttpServletRequest request) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        LoginMember loginMember = getLoginMember(request);
        if (loginMember == null || loginMember.getRole() == MemberRole.ADMIN) {
            return;
        }

        if (memberDailyVisitRepository.existsByMember_IdAndVisitDate(loginMember.getId(), today)) {
            return;
        }

        Member member = memberRepository.findByIdAndStatus(loginMember.getId(), MemberStatus.ACTIVE)
                .filter(activeMember -> activeMember.getRole() == MemberRole.USER)
                .orElse(null);

        if (member == null) {
            return;
        }

        memberDailyVisitRepository.save(MemberDailyVisit.create(member, today));
    }

    private LoginMember getLoginMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object attribute = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (attribute instanceof LoginMember loginMember) {
            return loginMember;
        }

        return null;
    }

    private void increasePublicCookieIfNeeded(HttpServletRequest request, HttpServletResponse response) {
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