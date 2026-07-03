package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.dto.AdminNicknameHistorySearchCondition;
import com.gahih.domain.admin.dto.AdminNicknameReservationSearchCondition;
import com.gahih.domain.admin.enumtype.AdminNicknameHistorySearchType;
import com.gahih.domain.admin.enumtype.AdminNicknameHistorySortType;
import com.gahih.domain.admin.enumtype.AdminNicknameReservationSearchType;
import com.gahih.domain.admin.enumtype.AdminNicknameReservationSortType;
import com.gahih.domain.admin.service.AdminNicknameService;
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
@RequestMapping("/admin")
public class AdminNicknameController {

    private final AdminNicknameService adminNicknameService;

    @GetMapping("/nickname-histories")
    public String nicknameHistoryList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminNicknameHistorySearchCondition condition,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("historyPage", adminNicknameService.searchNicknameHistories(condition));
        model.addAttribute("searchTypes", AdminNicknameHistorySearchType.values());
        model.addAttribute("sortTypes", AdminNicknameHistorySortType.values());
        return "admin/nicknames/admin-nickname-history-list";
    }

    @GetMapping("/nickname-reservations")
    public String nicknameReservationList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminNicknameReservationSearchCondition condition,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("reservationPage", adminNicknameService.searchNicknameReservations(condition));
        model.addAttribute("searchTypes", AdminNicknameReservationSearchType.values());
        model.addAttribute("sortTypes", AdminNicknameReservationSortType.values());
        return "admin/nicknames/admin-nickname-reservation-list";
    }
}
