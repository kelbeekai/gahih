package com.gahih.domain.comment.service;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.comment.entity.CommentMention;
import com.gahih.domain.comment.repository.CommentMentionRepository;
import com.gahih.domain.comment.repository.CommentRepository;
import com.gahih.domain.member.entity.Member;
import com.gahih.domain.post.entity.Post;
import com.gahih.global.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentMentionService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([가-힣A-Za-z0-9_]{2,12})");

    /*
     * mention 정책 스위치
     *
     * 1) 제한 유지 예시 -> 변경 시 관련 파일 post-detail.html, post-detail-comment.js 자동 반영
     *    - MENTION_LIMIT_ENABLED = true
     *    - MAX_MENTION_COUNT = 3   // 현재 정책
     *    - MAX_MENTION_COUNT = 10  // 향후 확장 예시
     *
     * 2) 제한 없음 예시 -> 변경 시 관련 파일 post-detail.html, post-detail-comment.js 자동 반영
     *    - MENTION_LIMIT_ENABLED = false
     *    - MAX_MENTION_COUNT 값은 화면 표시/문서화 용도 외에는 사용되지 않음
     */
    private static final boolean MENTION_LIMIT_ENABLED = true;
    private static final int MAX_MENTION_COUNT = 3;

    private final CommentMentionRepository commentMentionRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void syncMentions(Comment comment) {
        commentMentionRepository.deleteAllByCommentId(comment.getId());
        commentMentionRepository.flush();

        if (!isMentionAllowed(comment.getPost())) {
            return;
        }

        Map<String, Member> allowedMentionMemberMap =
                getAllowedMentionMemberMap(comment.getPost(), comment.getMember());

        if (allowedMentionMemberMap.isEmpty()) {
            return;
        }

        List<MentionToken> mentionTokens = extractMentionTokens(
                comment.getContent(),
                allowedMentionMemberMap.keySet()
        );

        if (mentionTokens.isEmpty()) {
            return;
        }

        Set<String> uniqueMentionNicknames = mentionTokens.stream()
                .map(MentionToken::nickname)
                .filter(allowedMentionMemberMap::containsKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (isMentionLimitEnabled() && uniqueMentionNicknames.size() > MAX_MENTION_COUNT) {
            throw new DomainValidationException("댓글에서 태그는 최대 " + MAX_MENTION_COUNT + "명까지만 가능합니다.");
        }

        Set<Long> savedMemberIds = new HashSet<>();

        for (MentionToken token : mentionTokens) {
            Member mentionedMember = allowedMentionMemberMap.get(token.nickname());
            if (mentionedMember == null) {
                continue;
            }

            if (!savedMemberIds.add(mentionedMember.getId())) {
                continue;
            }

            commentMentionRepository.save(
                    CommentMention.create(
                            comment,
                            mentionedMember,
                            token.nickname(),
                            token.startIndex(),
                            token.endIndexExclusive()
                    )
            );
        }
    }

    private record MentionToken(
            String nickname,
            int startIndex,
            int endIndexExclusive
    ) {
    }

    public String renderMentionContent(Comment comment) {
        String content = comment.getContent();
        if (content == null || content.isBlank()) {
            return "";
        }

        List<CommentMention> mentions = commentMentionRepository.findAllByCommentIdOrderByMentionStartIndexAsc(comment.getId());
        if (mentions.isEmpty()) {
            return escapeHtml(content);
        }

        List<CommentMention> sortedMentions = mentions.stream()
                .sorted(Comparator.comparing(CommentMention::getMentionStartIndex))
                .toList();

        StringBuilder sb = new StringBuilder();
        int cursor = 0;

        for (CommentMention mention : sortedMentions) {
            int start = mention.getMentionStartIndex();
            int end = mention.getMentionEndIndexExclusive();

            if (start < cursor || start < 0 || end > content.length() || start >= end) {
                continue;
            }

            sb.append(escapeHtml(content.substring(cursor, start)));

            String display = "@" + resolveMentionDisplayNickname(mention.getMentionedMember());
            sb.append("<span class=\"comment-mention\">")
                    .append(escapeHtml(display))
                    .append("</span>");

            cursor = end;
        }

        sb.append(escapeHtml(content.substring(cursor)));
        return sb.toString();
    }

    private String resolveMentionNickname(String rawNickname, Set<String> allowedNicknames) {
        if (rawNickname == null || rawNickname.isBlank() || allowedNicknames == null || allowedNicknames.isEmpty()) {
            return rawNickname;
        }

        if (allowedNicknames.contains(rawNickname)) {
            return rawNickname;
        }

        return allowedNicknames.stream()
                .filter(rawNickname::startsWith)
                .max(Comparator.comparingInt(String::length))
                .orElse(rawNickname);
    }

    private List<MentionToken> extractMentionTokens(String content, Set<String> allowedNicknames) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        Matcher matcher = MENTION_PATTERN.matcher(content);
        List<MentionToken> results = new ArrayList<>();

        while (matcher.find()) {
            String rawNickname = matcher.group(1);
            String nickname = resolveMentionNickname(rawNickname, allowedNicknames);

            int endIndexExclusive = matcher.start() + 1 + nickname.length();

            results.add(new MentionToken(
                    nickname,
                    matcher.start(),
                    endIndexExclusive
            ));
        }

        return results;
    }

    private Map<String, Member> getAllowedMentionMemberMap(Post post, Member commentWriter) {
        if (!canUseMentionInPost(post, commentWriter)) {
            return Map.of();
        }

        Map<Long, Member> participantMap = new LinkedHashMap<>();

        Member postWriter = post.getMember();
        if (isMentionTargetAllowed(postWriter, commentWriter, post)) {
            participantMap.put(postWriter.getId(), postWriter);
        }

        List<Member> commentWriters = commentRepository.findAllByPostIdOrderByIdAsc(post.getId())
                .stream()
                .map(Comment::getMember)
                .toList();

        for (Member member : commentWriters) {
            if (isMentionTargetAllowed(member, commentWriter, post)) {
                participantMap.put(member.getId(), member);
            }
        }

        return participantMap.values().stream()
                .collect(Collectors.toMap(
                        Member::getNickname,
                        member -> member,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private boolean isMentionTargetAllowed(Member targetMember, Member commentWriter, Post post) {
        if (targetMember == null || commentWriter == null || post == null) {
            return false;
        }

        if (!isMentionTargetAllowedInSecretPost(post, targetMember)) {
            return false;
        }

        if (targetMember.getId().equals(commentWriter.getId())) {
            return false;
        }

        boolean inquiryPost = post.getCategory().isCode(CategoryCode.INQUIRY);

        if (targetMember.isAdmin()) {
            return inquiryPost && targetMember.isActive();
        }

        if (targetMember.isActive()) {
            return true;
        }

        boolean writerAdmin = commentWriter.isAdmin();

        return writerAdmin && inquiryPost && targetMember.isSuspended();
    }

    private boolean canUseMentionInPost(Post post, Member commentWriter) {
        if (post == null || commentWriter == null) {
            return false;
        }

        if (!post.getCategory().isCommentAllowed()) {
            return false;
        }

        if (!post.isSecret()) {
            return true;
        }

        if (commentWriter.isAdmin()) {
            return true;
        }

        return post.getMember().getId().equals(commentWriter.getId());
    }

    private boolean isMentionTargetAllowedInSecretPost(Post post, Member targetMember) {
        if (post == null || targetMember == null) {
            return false;
        }

        if (!post.isSecret()) {
            return true;
        }

        return targetMember.isAdmin()
                || post.getMember().getId().equals(targetMember.getId());
    }

    private String resolveMentionDisplayNickname(Member member) {
        if (member.isWithdrawn() || member.isDeleted()) {
            return "탈퇴한 회원";
        }
        return member.getNickname();
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean isMentionAllowed(Post post) {
        return post != null && post.getCategory().isCommentAllowed();
    }

    // 마이페이지 나를 언급한 댓글 카드 미리보기에서 태그 부분은 빼고 진짜 내용만 보여주기
    public String stripLeadingMentions(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String trimmed = content.trim();
        String withoutLeadingMentions = trimmed.replaceFirst("^(?:@[가-힣A-Za-z0-9_]{2,12}\\s*)+", "").trim();

        if (withoutLeadingMentions.isBlank()) {
            return trimmed;
        }
        return withoutLeadingMentions;
    }

    public List<String> findMentionableCommentWriterNicknamesForPost(Long postId, Member commentWriter) {
        if (postId == null || commentWriter == null) {
            return List.of();
        }

        List<Comment> comments = commentRepository.findAllByPostIdOrderByIdAsc(postId);
        if (comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(Comment::getMember)
                .filter(member -> isMentionTargetAllowed(member, commentWriter, comments.get(0).getPost()))
                .map(Member::getNickname)
                .distinct()
                .toList();
    }

    public boolean isMentionLimitEnabled() {
        return MENTION_LIMIT_ENABLED;
    }

    public int getMaxMentionCount() {
        return MAX_MENTION_COUNT;
    }

    public String getMentionRegexForClient() {
        return "@([가-힣A-Za-z0-9_]{2,12})";
    }
}