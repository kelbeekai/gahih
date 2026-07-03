package com.gahih.domain.comment.service;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.comment.dto.CommentReactionResult;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.entity.CommentReaction;
import com.gahih.domain.comment.repository.CommentReactionRepository;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.reaction.enumtype.ReactionType;
import com.gahih.global.exception.ForbiddenException;
import com.gahih.global.exception.NotFoundException;
import com.gahih.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReactionService {

    private final CommentReactionRepository commentReactionRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentReactionResult react(
            String communityCode,
            Long loginMemberId,
            Long commentId,
            ReactionType requestedReactionType
    ) {
        validateRequestedReactionType(requestedReactionType);

        Member member = memberRepository.findByIdAndStatus(loginMemberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new UnauthorizedException("유효한 로그인 회원이 아닙니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        validateCommentInCommunity(comment, communityCode);
        validateCommentReactionAllowed(comment);

        CommentReaction existingReaction = commentReactionRepository.findByCommentIdAndMemberId(commentId, loginMemberId)
                .orElse(null);

        ReactionType before = existingReaction == null ? null : existingReaction.getReactionType();
        ReactionType after = resolveNextReactionType(before, requestedReactionType);

        if (existingReaction == null) {
            if (after != null) {
                commentReactionRepository.save(CommentReaction.create(member, comment, after));
            }
        } else {
            if (after == null) {
                commentReactionRepository.delete(existingReaction);
            } else {
                existingReaction.changeReactionType(after);
            }
        }

        comment.applyReactionChange(before, after);

        return CommentReactionResult.of(comment, after);
    }

    public ReactionType findMyReactionType(Long commentId, Long loginMemberId) {
        if (loginMemberId == null) {
            return null;
        }

        return commentReactionRepository.findByCommentIdAndMemberId(commentId, loginMemberId)
                .map(CommentReaction::getReactionType)
                .orElse(null);
    }

    private ReactionType resolveNextReactionType(ReactionType currentReactionType, ReactionType requestedReactionType) {
        if (currentReactionType == requestedReactionType) {
            return null; // 같은 버튼 다시 누르면 취소
        }
        return requestedReactionType;
    }

    private void validateRequestedReactionType(ReactionType requestedReactionType) {
        if (requestedReactionType == null) {
            throw new IllegalArgumentException("반응 타입은 필수입니다.");
        }
    }

    private void validateCommentReactionAllowed(Comment comment) {
        Category category = comment.getPost().getCategory();

        if (!category.isReactionAllowed()) {
            throw new ForbiddenException("해당 게시판은 댓글 좋아요/싫어요 반응이 허용되지 않습니다.");
        }
    }

    private void validateCommentInCommunity(Comment comment, String communityCode) {
        if (communityCode == null || communityCode.isBlank()) {
            throw new ForbiddenException("국가 커뮤니티 정보가 없습니다.");
        }

        if (!comment.getPost().getCategory().getCountryCommunity().isCode(communityCode)) {
            throw new NotFoundException("존재하지 않는 댓글입니다.");
        }
    }
}