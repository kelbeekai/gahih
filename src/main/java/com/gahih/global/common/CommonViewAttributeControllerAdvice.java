package com.gahih.global.common;

import com.gahih.domain.community.dto.CountryCommunityResponse;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.visit.dto.VisitorStatisticsSummary;
import com.gahih.domain.visit.service.VisitorStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class CommonViewAttributeControllerAdvice {

    private final VisitorStatisticsService visitorStatisticsService;
    private final CountryCommunityService countryCommunityService;

    @ModelAttribute("globalCommunities")
    public List<CountryCommunityResponse> globalCommunities() {
        return countryCommunityService.findEnabledCommunities();
    }

    @ModelAttribute("visitorStatistics")
    public VisitorStatisticsSummary visitorStatistics() {
        return visitorStatisticsService.getSummary();
    }
}