package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.dto.AdminLogSearchCondition;
import com.gahih.domain.admin.enumtype.AdminLogPeriodType;
import com.gahih.domain.admin.enumtype.AdminLogSearchType;
import com.gahih.domain.admin.enumtype.AdminLogSortType;
import com.gahih.domain.admin.enumtype.AdminLogTargetType;
import com.gahih.domain.admin.service.AdminLogService;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final AdminLogService adminLogService;
    private final CountryCommunityService countryCommunityService;

    @GetMapping
    public String logList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminLogSearchCondition condition,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("logPage", adminLogService.searchLogs(condition));
        model.addAttribute("searchTypes", AdminLogSearchType.values());
        model.addAttribute("sortTypes", AdminLogSortType.values());
        model.addAttribute("targetTypes", AdminLogTargetType.values());
        model.addAttribute("periodTypes", AdminLogPeriodType.values());
        model.addAttribute("actions", adminLogService.findDistinctLogActions());
        model.addAttribute("communities", countryCommunityService.findEnabledCommunities());
        return "admin/logs/admin-log-list";
    }
}
