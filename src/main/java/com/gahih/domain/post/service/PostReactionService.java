package com.gahih.domain.post.service;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.dto.PostReactionResult;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostReaction;
import com.gahih.domain.post.repository.PostReactionRepository;
import com.gahih.domain.post.repository.PostRepository;
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
public class PostReactionService {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostReactionResult react(Long loginMemberId, Long postId, ReactionType requestedReactionType) {
        validateRequestedReactionType(requestedReactionType);

        Member member = memberRepository.findByIdAndStatus(loginMemberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new UnauthorizedException("유효한 로그인 회원이 아닙니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostReactionAllowed(post);

        PostReaction existingReaction = postReactionRepository.findByPostIdAndMemberId(postId, loginMemberId)
                .orElse(null);

        ReactionType before = existingReaction == null ? null : existingReaction.getReactionType();
        ReactionType after = resolveNextReactionType(before, requestedReactionType);

        if (existingReaction == null) {
            if (after != null) {
                postReactionRepository.save(PostReaction.create(member, post, after));
            }
        } else {
            if (after == null) {
                postReactionRepository.delete(existingReaction);
            } else {
                existingReaction.changeReactionType(after);
            }
        }

        post.applyReactionChange(before, after);

        return PostReactionResult.of(post, after);
    }

    public ReactionType findMyReactionType(Long postId, Long loginMemberId) {
        if (loginMemberId == null) {
            return null;
        }

        return postReactionRepository.findByPostIdAndMemberId(postId, loginMemberId)
                .map(PostReaction::getReactionType)
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

    private void validatePostReactionAllowed(Post post) {
        Category category = post.getCategory();

        if (!category.isReactionAllowed()) {
            throw new ForbiddenException("해당 게시판은 좋아요/싫어요 반응이 허용되지 않습니다.");
        }
    }
}