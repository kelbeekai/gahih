package com.gahih.domain.admin.service;

import com.gahih.domain.admin.dto.*;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.member.repository.MemberRepository;
import com.gahih.domain.post.repository.PostRepository;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberQueryService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 페이지네이션 적용 전 관리자 게시글 목록 메서드
     */
    public List<AdminPostResponse> findAllPosts() {
        return postRepository.findAllByOrderByIdDesc()
                .stream()
                .map(AdminPostResponse::from)
                .toList();
    }

    public Page<AdminMemberResponse> searchMembers(AdminMemberSearchCondition condition) {
        return memberRepository.searchAdminMemberPage(
                condition.getKeywordOrNull(),
                condition.getSearchType(),
                condition.getRole(),
                condition.getStatus(),
                condition.getSortName(),
                PageRequest.of(condition.getSafePage() - 1, condition.getSafeSize())
        );
    }

    public AdminMemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        List<AdminMemberDetailPostResponse> recentPosts = postRepository
                .findTop5ByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(AdminMemberDetailPostResponse::from)
                .toList();

        List<AdminMemberDetailCommentResponse> recentComments = commentRepository
                .findTop5ByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(AdminMemberDetailCommentResponse::from)
                .toList();

        return AdminMemberDetailResponse.from(member, recentPosts, recentComments);
    }

}
