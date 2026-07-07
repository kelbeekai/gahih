package com.gahih.domain.post.controller;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.comment.dto.CommentCreateRequest;
import com.gahih.domain.comment.dto.CommentResponse;
import com.gahih.domain.comment.dto.CommentSearchCondition;
import com.gahih.domain.comment.enumtype.CommentSortType;
import com.gahih.domain.comment.service.CommentMentionService;
import com.gahih.domain.comment.service.CommentListService;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.service.account.MemberAccountService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.post.dto.PostDetailContext;
import com.gahih.domain.post.dto.PostDetailResponse;
import com.gahih.domain.post.dto.PostNavigationResponse;
import com.gahih.domain.post.dto.PostSearchCondition;
import com.gahih.domain.post.service.PostDetailContextService;
import com.gahih.domain.post.service.PostListService;
import com.gahih.domain.post.service.PostDetailService;
import com.gahih.global.argumentresolver.Login;
import com.gahih.global.util.LoginRedirectHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c/{communityCode}/posts")
public class PostDetailController {

    private final PostDetailService postDetailService;
    private final PostListService postListService;
    private final PostDetailContextService postDetailContextService;
    private final CategoryService categoryService;
    private final CommentListService commentListService;
    private final CommentMentionService commentMentionService;
    private final MemberAccountService memberAccountService;
    private final PostRedirectPathBuilder postRedirectPathBuilder;

    @GetMapping("/{postId}")
    public String detail(
            @PathVariable String communityCode,
            @PathVariable Long postId,
            @RequestParam(name = "fromCreate", defaultValue = "false") boolean fromCreate,
            @ModelAttribute("detailContext") PostDetailContext detailContext,
            @RequestParam(name = "commentSort", required = false) CommentSortType commentSort,
            @RequestParam(name = "commentPage", defaultValue = "1") int commentPage,
            @RequestParam(name = "mention", required = false) String mention,
            @RequestParam(name = "focus", required = false) String focus,
            @Login LoginMember loginMember,
            HttpServletRequest request,
            Model model
    ) {
        Long loginMemberId = loginMember != null ? loginMember.getId() : null;
        boolean isAdmin = loginMember != null && loginMember.getRole() == MemberRole.ADMIN;

        boolean adminOriginalVisible = isAdmin && detailContext.isAdminPostsSource();

        PostDetailResponse postDetail = postDetailService.findPostDetail(
                communityCode,
                postId,
                loginMemberId,
                isAdmin,
                adminOriginalVisible,
                fromCreate,
                request
        );

        PostSearchCondition postCondition = detailContext.toPostSearchCondition();
        postCondition.setCommunityCode(communityCode);

        model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
        model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        model.addAttribute("communityCode", communityCode);
        model.addAttribute("postDetail", postDetail);
        model.addAttribute("loginMember", loginMember);
        CommentSearchCondition commentCondition = new CommentSearchCondition();
        if (commentSort != null) {
            commentCondition.setSort(commentSort);
        }
        commentCondition.setPage(commentPage);

        Page<CommentResponse> commentPageResult = postDetail.isViewable()
                ? commentListService.searchCommentsByPostId(postId, loginMemberId, commentCondition)
                : Page.empty();

        model.addAttribute("commentPage", commentPageResult);
        model.addAttribute("comments", commentPageResult.getContent());
        model.addAttribute("commentCondition", commentCondition);

        LinkedHashSet<String> mentionableNicknames = new LinkedHashSet<>();

        if (loginMember != null && postDetail.isViewable() && postDetail.isCommentAllowed()) {
            Member commentWriter = memberAccountService.getMember(loginMember.getId());

            if (postDetail.isMentionableWriter()) {
                mentionableNicknames.add(postDetail.getWriterNickname());
            }

            mentionableNicknames.addAll(
                    commentMentionService.findMentionableCommentWriterNicknamesForPost(postId, commentWriter)
            );
        }

        /*
         * 프론트 mention 검증용 대상 목록.
         * 최대 mention 인원 정책(MAX_MENTION_COUNT / 제한 없음)은 CommentMentionService + post-detail.html + comment.js에서 처리한다.
         * 여기서는 "누가 mention 가능한가"만 내려준다.
         */

        model.addAttribute("mentionableNicknames", List.copyOf(mentionableNicknames));

        CommentCreateRequest commentCreateRequest = new CommentCreateRequest();
        if (loginMember != null
                && mention != null
                && !mention.isBlank()
                && mentionableNicknames.contains(mention)) {
            commentCreateRequest.setContent("@" + mention + " ");
        }

        model.addAttribute("commentCreateRequest", commentCreateRequest);
        model.addAttribute("focusComment", "comment".equalsIgnoreCase(focus));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("fromCreate", fromCreate);
        model.addAttribute("detailContext", detailContext);
        model.addAttribute("reportRedirectUrl", postRedirectPathBuilder.detailPath(communityCode, postId, fromCreate, detailContext));
        model.addAttribute("listRedirectUrl", postRedirectPathBuilder.listPath(communityCode, detailContext));
        model.addAttribute("commentLoginRedirectUrl",
                LoginRedirectHelper.createLoginPath(
                        postRedirectPathBuilder.detailPathWithCommentState(communityCode, postId, fromCreate, detailContext, commentCondition, null, "comment")
                )
        );

        Category selectedContextCategory = categoryService.findByIdOrNull(detailContext.getCategoryId());
        model.addAttribute("selectedContextCategory", selectedContextCategory);

        if (detailContext.isPostListSource()) {
            model.addAttribute("postNavigation",
                    postDetailContextService.findPostNavigation(postId, postCondition, loginMemberId, isAdmin));
            model.addAttribute("detailPinnedPosts",
                    postListService.findPinnedPosts(postCondition, loginMemberId, isAdmin));
            model.addAttribute("detailPostPage",
                    postListService.searchPosts(postCondition, loginMemberId, isAdmin));

        } else if (detailContext.isMyPostsSource()) {
            model.addAttribute("postNavigation",
                    postDetailContextService.findMyPostNavigation(communityCode, postId, loginMemberId, detailContext, loginMemberId, isAdmin));
            model.addAttribute("detailPinnedPosts", List.of());
            model.addAttribute("detailPostPage",
                    postDetailContextService.searchMyPostsForDetail(communityCode, loginMemberId, detailContext, loginMemberId, isAdmin));

        } else if (detailContext.isAdminPostsSource()) {
            model.addAttribute("postNavigation",
                    postDetailContextService.findAdminPostNavigation(communityCode, postId, detailContext, loginMemberId, isAdmin));
            model.addAttribute("detailPinnedPosts",
                    postDetailContextService.findAdminPinnedPostsForDetail(communityCode, detailContext, loginMemberId, isAdmin));
            model.addAttribute("detailPostPage",
                    postDetailContextService.searchAdminPostsForDetail(communityCode, detailContext, loginMemberId, isAdmin));

        } else {
            model.addAttribute("postNavigation", PostNavigationResponse.empty());
            model.addAttribute("selectedContextCategory", null);
            model.addAttribute("detailPinnedPosts", List.of());
            model.addAttribute("detailPostPage", Page.empty());
        }

        model.addAttribute("currentDetailPostId", postId);

        return "posts/post-detail";
    }
}
