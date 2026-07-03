package com.gahih.domain.post.controller;

import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.post.service.PostTradeService;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts")
public class PostTradeController {

    private final PostTradeService postTradeService;
    private final PostRedirectPathBuilder postRedirectPathBuilder;

    @PostMapping("/{postId}/trade-status/toggle")
    public String toggleTradeStatus(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            HttpServletRequest request
    ) {
        String redirectPath = postRedirectPathBuilder.detailPath(
                communityCode,
                postId,
                fromCreate,
                detailContext
        );

        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(request, redirectPath);
        }

        postTradeService.toggleStatus(communityCode, loginMember.getId(), postId);

        return "redirect:" + redirectPath;
    }
}