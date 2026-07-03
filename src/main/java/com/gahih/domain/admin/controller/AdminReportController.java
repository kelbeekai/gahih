package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.service.AdminReportService;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.report.dto.AdminReportSearchCondition;
import com.gahih.domain.report.dto.AdminReporterActivitySearchCondition;
import com.gahih.domain.report.enumtype.*;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final CategoryService categoryService;
    private final CountryCommunityService countryCommunityService;

    @GetMapping("/admin/reports")
    public String globalReportList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminReportSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(null);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("reportPage", adminReportService.searchReports(condition));
        model.addAttribute("targetTypes", ReportTargetType.values());
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("searchTypes", AdminReportSearchType.values());
        model.addAttribute("sortTypes", AdminReportSortType.values());
        return "admin/reports/admin-report-list";
    }

    @GetMapping("/admin/reporter-activities")
    public String reporterActivityList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminReporterActivitySearchCondition condition,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("activityPage", adminReportService.searchReporterActivities(condition));
        model.addAttribute("searchTypes", AdminReporterActivitySearchType.values());
        model.addAttribute("sortTypes", AdminReporterActivitySortType.values());
        model.addAttribute("communities", countryCommunityService.findEnabledCommunities());
        return "admin/reports/admin-reporter-activity-list";
    }

    @PostMapping("/admin/reports/{targetType}/{targetId}/no-action")
    public String globalMarkReportNoActionNeeded(
            @Login LoginMember loginMember,
            @PathVariable ReportTargetType targetType,
            @PathVariable Long targetId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String targetTypeFilter,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminReportService.markReportNoActionNeeded(loginMember.getId(), targetType, targetId, reason);
        addAdminReportSearchRedirectAttributes(targetTypeFilter, status, searchType, keyword, sort, size, page, redirectAttributes);
        return "redirect:/admin/reports";
    }

    @GetMapping("/c/{communityCode}/admin/reports")
    public String reportList(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminReportSearchCondition condition,
            Model model
    ) {
        condition.setCommunityCode(communityCode);

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("reportPage", adminReportService.searchReports(condition));
        model.addAttribute("targetTypes", ReportTargetType.values());
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("searchTypes", AdminReportSearchType.values());
        model.addAttribute("sortTypes", AdminReportSortType.values());

        return "admin/reports/admin-report-list";
    }

    @PostMapping("/c/{communityCode}/admin/reports/{targetType}/{targetId}/no-action")
    public String markReportNoActionNeeded(
            @PathVariable String communityCode,
            @Login LoginMember loginMember,
            @PathVariable ReportTargetType targetType,
            @PathVariable Long targetId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String targetTypeFilter,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminReportService.markReportNoActionNeeded(loginMember.getId(), targetType, targetId, reason);
        addAdminReportSearchRedirectAttributes(targetTypeFilter, status, searchType, keyword, sort, size, page, redirectAttributes);
        return "redirect:/c/" + communityCode + "/admin/reports";
    }

    private void addAdminReportSearchRedirectAttributes(
            String targetType,
            String status,
            String searchType,
            String keyword,
            String sort,
            Integer size,
            Integer page,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("targetType", targetType);
        redirectAttributes.addAttribute("status", status);
        redirectAttributes.addAttribute("searchType", searchType);
        redirectAttributes.addAttribute("keyword", (keyword == null || keyword.isBlank()) ? null : keyword.trim());
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("size", size == null ? 20 : size);
        redirectAttributes.addAttribute("page", page == null || page < 1 ? 1 : page);
    }
}