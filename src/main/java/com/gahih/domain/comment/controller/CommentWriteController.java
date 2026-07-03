package com.gahih.domain.comment.controller;

import com.gahih.domain.comment.dto.CommentCreateRequest;
import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.comment.dto.CommentUpdateRequest;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.comment.service.CommentService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts/{postId}/comments")
public class CommentWriteController {

    private final CommentRedirectPathBuilder commentRedirectPathBuilder;
    private final CommentService commentService;

    @PostMapping
    public String createComment(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage,
            @Login LoginMember loginMember,
            @Valid @ModelAttribute("commentCreateRequest") CommentCreateRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest
    ) {
        CommentSearchCondition commentCondition = new CommentSearchCondition();
        if (commentSort != null) {
            commentCondition.setSort(commentSort);
        }
        commentCondition.setPage(commentPage);

        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(
                    httpServletRequest,
                    commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition)
            );
        }

        if (bindingResult.hasErrors()) {
            return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
        }

        commentService.createComment(communityCode, loginMember.getId(), postId, request);
        return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
    }

    @PostMapping("/{commentId}/edit")
    public String updateComment(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage,
            @Login LoginMember loginMember,
            @Valid @ModelAttribute("commentUpdateRequest") CommentUpdateRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest
    ) {
        CommentSearchCondition commentCondition = new CommentSearchCondition();
        if (commentSort != null) {
            commentCondition.setSort(commentSort);
        }
        commentCondition.setPage(commentPage);

        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(
                    httpServletRequest,
                    commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition)
            );
        }

        if (bindingResult.hasErrors()) {
            return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
        }

        commentService.updateComment(communityCode, loginMember.getId(), postId, commentId, request);
        return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage,
            @Login LoginMember loginMember,
            HttpServletRequest httpServletRequest
    ) {
        CommentSearchCondition commentCondition = new CommentSearchCondition();
        if (commentSort != null) {
            commentCondition.setSort(commentSort);
        }
        commentCondition.setPage(commentPage);

        if (loginMember == null) {
            return "redirect:" + LoginRedirectHelper.createLoginPathForPost(
                    httpServletRequest,
                    commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition)
            );
        }

        commentService.deleteComment(communityCode, loginMember.getId(), postId, commentId);
        return "redirect:" + commentRedirectPathBuilder.detailPathWithCommentAnchor(communityCode, postId, fromCreate, detailContext, commentCondition);
    }

}