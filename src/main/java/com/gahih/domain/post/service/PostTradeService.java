package com.gahih.domain.post.service;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.post.entity.Post;
import com.gahih.domain.post.entity.PostTradeInfo;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.domain.post.repository.PostTradeInfoRepository;
import com.gahih.global.exception.BusinessException;
import com.gahih.global.exception.ForbiddenException;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostTradeService {

    private final PostTradeInfoRepository postTradeInfoRepository;
    private final PostRepository postRepository;

    @Transactional
    public void syncForCreate(Post post, TradeType tradeType) {
        if (!isMarketPost(post)) {
            validateTradeTypeEmptyForNormalPost(tradeType);
            return;
        }

        validateTradeTypeRequired(tradeType);
        postTradeInfoRepository.save(PostTradeInfo.create(post, tradeType));
    }

    @Transactional
    public void syncForUpdate(Post post, TradeType tradeType) {
        if (!isMarketPost(post)) {
            validateTradeTypeEmptyForNormalPost(tradeType);
            postTradeInfoRepository.deleteByPostId(post.getId());
            return;
        }

        validateTradeTypeRequired(tradeType);

        postTradeInfoRepository.findByPostId(post.getId())
                .ifPresentOrElse(
                        tradeInfo -> tradeInfo.changeTypeForUpdate(tradeType),
                        () -> postTradeInfoRepository.save(PostTradeInfo.create(post, tradeType))
                );
    }

    @Transactional
    public void toggleStatus(String communityCode, Long loginMemberId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        validatePostInCommunity(post, communityCode);
        validateOwner(post, loginMemberId);
        validateActivePost(post);
        validateMarketPost(post);

        PostTradeInfo tradeInfo = postTradeInfoRepository.findByPostId(postId)
                .orElseThrow(() -> new BusinessException("거래 정보가 없는 게시글입니다."));

        tradeInfo.toggleStatus();
    }

    public PostTradeInfo findByPostIdOrNull(Long postId) {
        return postTradeInfoRepository.findByPostId(postId).orElse(null);
    }

    private boolean isMarketPost(Post post) {
        return post != null && post.getCategory().isCode(CategoryCode.MARKET);
    }

    private void validateTradeTypeRequired(TradeType tradeType) {
        if (tradeType == null) {
            throw new BusinessException("나눔·매매 게시판에서는 원하시는 거래 유형을 선택해야 합니다.");
        }
    }

    private void validateTradeTypeEmptyForNormalPost(TradeType tradeType) {
        if (tradeType != null) {
            throw new BusinessException("나눔·매매 게시판이 아닌 글에는 거래 유형을 설정할 수 없습니다.");
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

    private void validateOwner(Post post, Long loginMemberId) {
        if (loginMemberId == null || !post.getMember().getId().equals(loginMemberId)) {
            throw new ForbiddenException("작성자만 거래 상태를 변경할 수 있습니다.");
        }
    }

    private void validateActivePost(Post post) {
        if (!post.isActive()) {
            throw new BusinessException("삭제 또는 블라인드 처리된 게시글의 거래 상태는 변경할 수 없습니다.");
        }
    }

    private void validateMarketPost(Post post) {
        if (!isMarketPost(post)) {
            throw new BusinessException("나눔·매매 게시글만 거래 상태를 변경할 수 있습니다.");
        }
    }
}