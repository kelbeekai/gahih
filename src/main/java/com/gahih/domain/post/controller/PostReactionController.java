package com.gahih.domain.post.controller;

import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.post.service.PostReactionService;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.util.LoginRedirectHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts")
public class PostReactionController {

    private final PostReactionService postReactionService;
    private final PostRedirectPathBuilder postRedirectPathBuilder;

    @PostMapping("/{postId}/like")
    public String likePost(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            RedirectAttributes redirectAttributes
    ) {
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "좋아요/싫어요는 로그인 후 이용할 수 있습니다.");
            return "redirect:" + LoginRedirectHelper.createLoginPath(
                    postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext)
            );
        }

        postReactionService.react(loginMember.getId(), postId, ReactionType.LIKE);
        return "redirect:" + postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext);
    }

    @PostMapping("/{postId}/dislike")
    public String dislikePost(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @Login LoginMember loginMember,
            RedirectAttributes redirectAttributes
    ) {
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "좋아요/싫어요는 로그인 후 이용할 수 있습니다.");
            return "redirect:" + LoginRedirectHelper.createLoginPath(
                    postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext)
            );
        }

        postReactionService.react(loginMember.getId(), postId, ReactionType.DISLIKE);
        return "redirect:" + postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext);
    }
}