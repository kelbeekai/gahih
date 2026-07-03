package com.gahih.domain.report.controller;

import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.report.dto.ReportCreateRequest;
import com.gahih.domain.report.service.ReportService;
import com.gahih.global.argumentresolver.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public String createReport(
            @Login LoginMember loginMember,
            @Valid @ModelAttribute ReportCreateRequest request,
            @ModelAttribute("redirectUrl") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        reportService.createReport(loginMember.getId(), request, resolveCommunityCodeFromRedirectUrl(redirectUrl));
        redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다.");
        return "redirect:" + normalizeRedirectUrl(redirectUrl);
    }

    private String normalizeRedirectUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return "/";
        }

        String trimmed = redirectUrl.trim();
        if (!trimmed.startsWith("/")) {
            return "/";
        }

        return trimmed;
    }

    private String resolveCommunityCodeFromRedirectUrl(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return null;
        }

        String trimmed = redirectUrl.trim();

        if (!trimmed.startsWith("/c/")) {
            return null;
        }

        String[] parts = trimmed.split("/");
        if (parts.length < 3) {
            return null;
        }

        String communityCode = parts[2];
        if (communityCode == null || communityCode.isBlank()) {
            return null;
        }

        return communityCode.toUpperCase();
    }
}