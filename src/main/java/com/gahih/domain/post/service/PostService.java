package com.gahih.domain.post.service;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.dto.*;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostTradeInfo;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.exception.ForbiddenException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PostAttachmentService postAttachmentService;
    private final PostTradeService postTradeService;

    @Transactional
    public Long createPost(String communityCode, Long loginMemberId, PostCreateRequest request) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new UnauthorizedException("유효한 로그인 회원이 아닙니다."));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));

        validateCategoryInCommunity(category, communityCode);
        validatePostWriteAllowed(member, category);
        validateSecretPostAllowed(category, request.isSecret());

        Post post = Post.create(
                member,
                category,
                request.getTitle(),
                request.getContent(),
                request.isSecret()
        );

        Post savedPost = postRepository.save(post);
        postTradeService.syncForCreate(savedPost, request.getTradeType());
        postAttachmentService.saveAttachments(savedPost, request.getAttachments());
        return savedPost.getId();
    }

    @Transactional
    public void updatePost(String communityCode, Long loginMemberId, Long postId, PostUpdateRequest request) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new UnauthorizedException("유효한 로그인 회원이 아닙니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);
        validateOwner(loginMemberId, post);
        validateEditable(post);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));

        validateCategoryInCommunity(category, communityCode);
        validatePostWriteAllowed(member, category);
        validateSecretPostAllowed(category, request.isSecret());

        post.update(category, request.getTitle(), request.getContent(), request.isSecret());
        postTradeService.syncForUpdate(post, request.getTradeType());
        postAttachmentService.saveAttachments(post, request.getAttachments());
    }

    public PostUpdateRequest getPostUpdateForm(String communityCode, Long loginMemberId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);
        validateOwner(loginMemberId, post);
        validateEditable(post);

        PostUpdateRequest request = new PostUpdateRequest();
        request.setCategoryId(post.getCategory().getId());
        request.setTitle(post.getTitle());
        request.setContent(post.getContent());
        request.setSecret(post.isSecret());

        PostTradeInfo tradeInfo = postTradeService.findByPostIdOrNull(postId);
        if (tradeInfo != null) {
            request.setTradeType(tradeInfo.getType());
        }

        return request;
    }

    @Transactional
    public void deletePost(String communityCode, Long loginMemberId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);
        validateOwner(loginMemberId, post);
        validateEditable(post);

/*      물리 삭제 로직
        // 댓글, 반응이 남아 있으면 comment.post_id FK 때문에 게시글 삭제가 실패한다.
        // 같은 트랜잭션 안에서 자식 댓글을 먼저 삭제한 뒤 게시글을 삭제한다.
        commentReactionRepository.deleteAllByCommentPostId(postId);
        commentRepository.deleteAllByPostId(postId);
        postReactionRepository.deleteAllByPostId(postId);
        postAttachmentService.deleteAllByPost(post);
        postRepository.delete(post);
*/

        post.deleteByUser();
    }


    /**
     * 특정 카테고리만 고정 기능 제한 메서드
     */
/*
    public void validatePinAllowed(Post post) {
        if (!post.getCategory().isCode(CategoryCode.NOTICE)) {
            throw new ForbiddenException("공지사항 게시판 글만 고정할 수 있습니다.");
        }
    }
*/

    /**
     * 특정 카테고리만 비밀글 기능 제한 메서드
     */
    private void validateSecretPostAllowed(Category category, boolean secret) {
        if (!secret) {
            return;
        }

        if (!category.isCode(CategoryCode.INQUIRY)) {
            throw new ForbiddenException("비밀글은 이용문의 게시판에서만 작성할 수 있습니다.");
        }
    }

    private void validateOwner(Long loginMemberId, Post post) {
        if (!post.getMember().getId().equals(loginMemberId)) {
            throw new ForbiddenException("본인이 작성한 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    private void validatePostWriteAllowed(Member member, Category category) {
        if (member.isSuspended()) {
            if (!category.isCode(CategoryCode.INQUIRY)) {
                throw new ForbiddenException("이용 정지된 회원은 이용문의 게시판에만 글을 작성할 수 있습니다.");
            }
            return;
        }

        if (!member.isActive()) {
            throw new ForbiddenException("현재 회원 상태에서는 글을 작성할 수 없습니다.");
        }

        if (category.isAdminWriteOnly() && member.getRole() != MemberRole.ADMIN) {
            throw new ForbiddenException("해당 게시판은 관리자만 글을 작성할 수 있습니다.");
        }
    }

    private void validateEditable(Post post) {
        if (!post.isEditableByUser()) {
            throw new ForbiddenException("삭제 또는 블라인드 처리된 게시글은 수정/삭제할 수 없습니다.");
        }
    }

    private void validateCategoryInCommunity(Category category, String communityCode) {
        if (communityCode == null || communityCode.isBlank()) {
            throw new ForbiddenException("국가 커뮤니티 정보가 없습니다.");
        }

        if (!category.getCountryCommunity().isCode(communityCode)) {
            throw new ForbiddenException("현재 커뮤니티에 속하지 않는 카테고리입니다.");
        }
    }

    private void validatePostInCommunity(Post post, String communityCode) {
        if (communityCode == null || communityCode.isBlank()) {
            throw new ForbiddenException("국가 커뮤니티 정보가 없습니다.");
        }

        if (!post.getCategory().getCountryCommunity().isCode(communityCode)) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }
    }
}
