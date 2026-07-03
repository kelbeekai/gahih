package com.gahih.domain.comment.service;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.comment.dto.CommentCreateRequest;
import com.gahih.domain.comment.dto.CommentUpdateRequest;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.entity.Post;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentMentionService commentMentionService;

    @Transactional
    public Long createComment(String communityCode, Long loginMemberId, Long postId, CommentCreateRequest request) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new UnauthorizedException("유효한 로그인 회원이 아닙니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);
        validateCommentAllowed(member, post);
        validateCommentTargetPost(post);

        Comment comment = Comment.create(
                post,
                member,
                request.getContent()
        );

        Comment savedComment = commentRepository.save(comment);
        commentMentionService.syncMentions(savedComment);
        return savedComment.getId();
    }

    @Transactional
    public void updateComment(
            String communityCode,
            Long loginMemberId,
            Long postId,
            Long commentId,
            CommentUpdateRequest request
    ) {
        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new UnauthorizedException("유효한 로그인 회원이 아닙니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        validateCommentBelongsToPost(comment, postId);
        validatePostInCommunity(comment.getPost(), communityCode);
        validateOwner(loginMemberId, comment);
        validateCommentAllowed(comment.getMember(), comment.getPost());
        validateEditable(comment);
        validateCommentTargetPost(comment.getPost());

        comment.updateContent(request.getContent());
        commentMentionService.syncMentions(comment);
    }

    @Transactional
    public void deleteComment(String communityCode, Long loginMemberId, Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        validateCommentBelongsToPost(comment, postId);
        validatePostInCommunity(comment.getPost(), communityCode);
        validateOwner(loginMemberId, comment);
        validateEditable(comment);

/*      물리 삭제 로직
        commentReactionRepository.deleteAllByCommentId(commentId);
        commentRepository.delete(comment);
*/
        comment.deleteByUser();
    }

    private void validateOwner(Long loginMemberId, Comment comment) {
        if (!comment.getMember().getId().equals(loginMemberId)) {
            throw new ForbiddenException("본인 댓글만 삭제할 수 있습니다.");
        }
    }

    private void validateCommentAllowed(Member member, Post post) {
        Category category = post.getCategory();

        if (!category.isCommentAllowed()) {
            throw new ForbiddenException("해당 게시판은 댓글 작성이 허용되지 않습니다.");
        }

        if (member.isSuspended()) {
            if (!category.isCode(CategoryCode.INQUIRY)) {
                throw new ForbiddenException("이용 정지된 회원은 이용문의 게시판에만 댓글을 작성할 수 있습니다.");
            }
            return;
        }

        if (!member.isActive()) {
            throw new ForbiddenException("현재 회원 상태에서는 댓글을 작성할 수 없습니다.");
        }
    }

    private void validateEditable(Comment comment) {
        if (!comment.isEditableByUser()) {
            throw new ForbiddenException("삭제 또는 블라인드 처리된 댓글은 수정/삭제할 수 없습니다.");
        }
    }

    private void validateCommentTargetPost(Post post) {
        if (!post.isActive()) {
            throw new ForbiddenException("삭제 또는 블라인드 처리된 게시글에는 댓글을 작성하거나 수정할 수 없습니다.");
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

    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("존재하지 않는 댓글입니다.");
        }
    }

}
