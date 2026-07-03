package com.gahih.domain.comment.controller;

import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.comment.service.CommentReactionService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.util.LoginRedirectHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommentReactionController {

    private final CommentRedirectPathBuilder commentRedirectPathBuilder;
    private final CommentReactionService commentReactionService;

    @PostMapping("/c/{communityCode}/comments/{commentId}/like")
    public String like(
            @PathVariable String communityCode,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage,
            @Login LoginMember loginMember,
            RedirectAttributes redirectAttributes
    ) {
        CommentSearchCondition commentCondition = new CommentSearchCondition();
        if (commentSort != null) {
            commentCondition.setSort(commentSort);
        }
        commentCondition.setPage(commentPage);

        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "좋아요/싫어요는 로그인 후 이용할 수 있습니다.");
            return "redirect:" + LoginRedirectHelper.createLoginPath(
                    commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition)
            );
        }

        commentReactionService.react(communityCode, loginMember.getId(), commentId, ReactionType.LIKE);
        return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
    }

    @PostMapping("/c/{communityCode}/comments/{commentId}/dislike")
    public String dislike(
            @PathVariable String communityCode,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage,
            @Login LoginMember loginMember,
            RedirectAttributes redirectAttributes
    ) {
        CommentSearchCondition commentCondition = new CommentSearchCondition();
        if (commentSort != null) {
            commentCondition.setSort(commentSort);
        }
        commentCondition.setPage(commentPage);

        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "좋아요/싫어요는 로그인 후 이용할 수 있습니다.");
            return "redirect:" + LoginRedirectHelper.createLoginPath(
                    commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition)
            );
        }

        commentReactionService.react(communityCode, loginMember.getId(), commentId, ReactionType.DISLIKE);
        return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
    }
}