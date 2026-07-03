package com.gahih.global.interceptor;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostAttachment;
import com.gahih.domain.post.repository.PostAttachmentRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberStatusPolicyInterceptor implements HandlerInterceptor {

    private static final Pattern POST_ID_PATTERN = Pattern.compile("^/c/[^/]+/posts/(\\d+)(?:/.*)?$");
    private static final Pattern ATTACHMENT_ID_PATTERN = Pattern.compile("^/c/[^/]+/posts/attachments/(\\d+)/(?:preview|download)$");
    private static final Pattern COMMENT_ID_PATTERN = Pattern.compile("^/c/[^/]+/comments/(\\d+)/(?:like|dislike)$");
    private static final Pattern POST_COMMENT_WRITE_PATTERN = Pattern.compile("^/c/[^/]+/posts/(\\d+)/comments$");

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final CommentRepository commentRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return true;
        }

        Member member = memberRepository.findById(loginMember.getId()).orElse(null);
        if (member == null) {

            log.warn(
                    "Deleted member session invalidated. memberId={}",
                    loginMember.getId()
            );

            session.invalidate();
            response.sendRedirect("/");
            return false;
        }

        session.setAttribute(SessionConst.LOGIN_MEMBER, new LoginMember(member));

        if (member.isDeleted()) {
            session.invalidate();
            response.sendRedirect("/");
            return false;
        }

        if (member.isWithdrawn()) {
            if (isAllowedForWithdrawn(request)) {
                return true;
            }

            log.warn(
                    "Blocked withdrawn member access. memberId={}, uri={}",
                    member.getId(),
                    request.getRequestURI()
            );

            response.sendRedirect("/members/withdrawn");
            return false;
        }

        if (member.isSuspended()) {
            if (member.isSuspensionExpired()) {
                member.releaseSuspension();
                session.setAttribute(SessionConst.LOGIN_MEMBER, new LoginMember(member));
                return true;
            }

            if (isAllowedForSuspended(request)) {
                return true;
            }

            log.warn(
                    "Blocked suspended member access. memberId={}, uri={}",
                    member.getId(),
                    request.getRequestURI()
            );

            response.sendRedirect("/members/suspended");
            return false;
        }

        return true;
    }

    private boolean isAllowedForWithdrawn(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return "/members/withdrawn".equals(uri)
                || "/members/restore".equals(uri)
                || "/members/logout".equals(uri);
    }

    private boolean isAllowedForSuspended(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method) && "/".equals(uri)) {
            return true;
        }

        if (uri.startsWith("/mypage") || uri.matches("^/c/[^/]+/mypage(?:/.*)?$")) {
            return true;
        }

        if ("/members/logout".equals(uri) || "/members/suspended".equals(uri)) {
            return true;
        }

        if (isInquiryRequest(request)) {
            return true;
        }

        return false;
    }

    private boolean isInquiryRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.matches("^/c/[^/]+/posts$")) {
            Long categoryId = parseLong(request.getParameter("categoryId"));
            return isInquiryCategoryId(categoryId);
        }

        if (uri.matches("^/c/[^/]+/posts/new$")) {
            Long categoryId = parseLong(request.getParameter("categoryId"));
            return isInquiryCategoryId(categoryId);
        }

        if (uri.matches("^/c/[^/]+/posts/new$") && "POST".equalsIgnoreCase(request.getMethod())) {
            Long categoryId = parseLong(request.getParameter("categoryId"));
            return isInquiryCategoryId(categoryId);
        }

        Matcher postMatcher = POST_ID_PATTERN.matcher(uri);
        if (postMatcher.matches()) {
            Long postId = parseLong(postMatcher.group(1));
            if (postId == null) {
                return false;
            }

            return isInquiryPostId(postId);
        }

        Matcher attachmentMatcher = ATTACHMENT_ID_PATTERN.matcher(uri);
        if (attachmentMatcher.matches()) {
            Long attachmentId = parseLong(attachmentMatcher.group(1));
            return isInquiryAttachmentId(attachmentId);
        }

        if (uri.matches("^/c/[^/]+/posts/\\d+/comments$")) {
            Long postId = extractPostIdFromCommentWritePath(uri);
            return isInquiryPostId(postId);
        }

        return false;
    }

    private Long extractPostIdFromCommentWritePath(String uri) {
        Matcher matcher = Pattern.compile("^/c/[^/]+/posts/(\\d+)/comments$").matcher(uri);
        if (!matcher.matches()) {
            return null;
        }
        return parseLong(matcher.group(1));
    }

    private boolean isInquiryCategoryId(Long categoryId) {
        if (categoryId == null) {
            return false;
        }

        Optional<Category> category = categoryRepository.findById(categoryId);
        return category.map(value -> value.isCode(CategoryCode.INQUIRY)).orElse(false);
    }

    private boolean isInquiryPostId(Long postId) {
        if (postId == null) {
            return false;
        }

        Optional<Post> post = postRepository.findById(postId);
        return post.map(value -> value.getCategory().isCode(CategoryCode.INQUIRY)).orElse(false);
    }

    private boolean isInquiryAttachmentId(Long attachmentId) {
        if (attachmentId == null) {
            return false;
        }

        Optional<PostAttachment> attachment = postAttachmentRepository.findById(attachmentId);
        return attachment.map(value -> value.getPost().getCategory().isCode(CategoryCode.INQUIRY)).orElse(false);
    }

    private boolean isInquiryCommentId(Long commentId) {
        if (commentId == null) {
            return false;
        }

        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.map(value -> value.getPost().getCategory().isCode(CategoryCode.INQUIRY)).orElse(false);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}