package com.gahih.domain.visit.service;

import com.gahih.domain.visit.dto.VisitorStatisticsSummary;
import com.gahih.domain.visit.entity.VisitorStatistics;
import com.gahih.domain.visit.repository.VisitorStatisticsRepository;
import com.gahih.global.policy.SessionCountPolicyService;
import com.gahih.global.policy.SessionCountPolicyType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitorStatisticsService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final VisitorStatisticsRepository visitorStatisticsRepository;
    private final SessionCountPolicyService sessionCountPolicyService;

    @Transactional
    public void increaseIfNeeded(HttpServletRequest request) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        long todayKey = today.toEpochDay();

        boolean shouldIncrease = sessionCountPolicyService.shouldIncreaseOncePerSession(
                request,
                SessionCountPolicyType.VISITOR_DAILY,
                todayKey
        );

        if (!shouldIncrease) {
            return;
        }

        VisitorStatistics statistics = visitorStatisticsRepository.findByVisitDate(today)
                .orElseGet(() -> visitorStatisticsRepository.save(VisitorStatistics.create(today)));

        statistics.increase();
    }

    public VisitorStatisticsSummary getSummary() {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        long todayCount = visitorStatisticsRepository.findByVisitDate(today)
                .map(VisitorStatistics::getDailyCount)
                .orElse(0L);

        long totalCount = visitorStatisticsRepository.sumTotalCount();

        return VisitorStatisticsSummary.of(todayCount, totalCount);
    }
}