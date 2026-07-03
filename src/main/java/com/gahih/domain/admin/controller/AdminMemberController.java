package com.gahih.domain.admin.controller;

import com.gahih.domain.admin.dto.AdminMemberSearchCondition;
import com.gahih.domain.admin.enumtype.AdminMemberSearchType;
import com.gahih.domain.admin.enumtype.AdminMemberSortType;
import com.gahih.domain.admin.service.AdminMemberService;
import com.gahih.domain.admin.service.AdminNicknameService;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;
    private final AdminNicknameService adminNicknameService;

    @GetMapping
    public String memberList(
            @Login LoginMember loginMember,
            @ModelAttribute("condition") AdminMemberSearchCondition condition,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("memberPage", adminMemberService.searchMembers(condition));
        model.addAttribute("roles", MemberRole.values());
        model.addAttribute("statuses", MemberStatus.values());
        model.addAttribute("searchTypes", AdminMemberSearchType.values());
        model.addAttribute("sortTypes", AdminMemberSortType.values());
        return "admin/members/admin-member-list";
    }

    @GetMapping("/{memberId}")
    public String memberDetail(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam(required = false) String returnUrl,
            Model model
    ) {
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("memberDetail", adminMemberService.getMemberDetail(memberId));
        model.addAttribute("memberDetailReturnUrl",
                (returnUrl == null || returnUrl.isBlank()) ? "/admin/members" : returnUrl);
        return "admin/members/admin-member-detail";
    }

    @PostMapping("/{memberId}/update-role")
    public String updateMemberRole(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam MemberRole role,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminMemberService.updateMemberRole(loginMember.getId(), memberId, role);
        return redirectToMemberDetailOrMemberList(memberId, returnUrl, searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes);
    }

    @PostMapping("/{memberId}/activate-member")
    public String activateMember(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminMemberService.activateMember(loginMember.getId(), memberId);
        return redirectToMemberDetailOrMemberList(memberId, returnUrl, searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes);
    }

    @PostMapping("/{memberId}/suspend-temporary")
    public String suspendMemberTemporarily(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam String suspendedUntil,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        LocalDateTime resolvedSuspendedUntil = parseSuspendedUntil(suspendedUntil);
        adminMemberService.suspendMemberTemporarily(loginMember.getId(), memberId, resolvedSuspendedUntil, reason);
        return redirectToMemberDetailOrMemberList(memberId, returnUrl, searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes);
    }

    @PostMapping("/{memberId}/suspend-permanent")
    public String suspendMemberPermanently(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            RedirectAttributes redirectAttributes
    ) {
        adminMemberService.suspendMemberPermanently(loginMember.getId(), memberId, reason);
        return redirectToMemberDetailOrMemberList(memberId, returnUrl, searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes);
    }

    @PostMapping("/{memberId}/finalize")
    public String finalizeMember(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        adminMemberService.finalizeMember(loginMember.getId(), memberId, reason);
        return redirectToMemberDetailOrMemberList(memberId, returnUrl, searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes);
    }

    @PostMapping("/{memberId}/force-nickname")
    public String forceNicknameChange(
            @Login LoginMember loginMember,
            @PathVariable Long memberId,
            @RequestParam(required = false) String returnUrl,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        adminNicknameService.forceChangeMemberNickname(loginMember.getId(), memberId, reason);
        return redirectToMemberDetailOrMemberList(memberId, returnUrl, searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes);
    }

    private void addAdminMemberSearchRedirectAttributes(
            String searchType,
            String keyword,
            String filterRole,
            String filterStatus,
            String sort,
            Integer size,
            Integer page,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("searchType", searchType);
        redirectAttributes.addAttribute("keyword", (keyword == null || keyword.isBlank()) ? null : keyword.trim());
        redirectAttributes.addAttribute("role", filterRole);
        redirectAttributes.addAttribute("status", filterStatus);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("size", size == null ? 20 : size);
        redirectAttributes.addAttribute("page", page == null || page < 1 ? 1 : page);
    }

    private LocalDateTime parseSuspendedUntil(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new BusinessException("정지 기간을 입력해주세요.");
        }

        String trimmed = rawValue.trim();

        if (trimmed.matches("^\\d+$")) {
            int days = Integer.parseInt(trimmed);
            if (days <= 0) {
                throw new BusinessException("정지 기간 일수는 1일 이상이어야 합니다.");
            }
            return LocalDateTime.now().plusDays(days);
        }

        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new BusinessException("정지 기간은 yyyy-MM-ddTHH:mm 형식 또는 자연수 일수로 입력해주세요.");
        }
    }

    private String redirectToMemberDetailOrMemberList(
            Long memberId,
            String returnUrl,
            String searchType,
            String keyword,
            String filterRole,
            String filterStatus,
            String sort,
            Integer size,
            Integer page,
            RedirectAttributes redirectAttributes
    ) {
        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:/admin/members/" + memberId
                    + "?returnUrl=" + UriUtils.encode(returnUrl, StandardCharsets.UTF_8);
        }

        addAdminMemberSearchRedirectAttributes(
                searchType, keyword, filterRole, filterStatus, sort, size, page, redirectAttributes
        );
        return "redirect:/admin/members";
    }
}
