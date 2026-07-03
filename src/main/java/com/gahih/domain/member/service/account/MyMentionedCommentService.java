package com.gahih.domain.member.service;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.comment.entity.CommentMention;
import com.gahih.domain.comment.repository.CommentMentionRepository;
import com.gahih.domain.member.dto.MyMentionedCommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyMentionedCommentService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int LOOKBACK_DAYS = 7;
    private static final int MAX_RESULT_SIZE = 5;
    private static final int PREVIEW_LENGTH = 60;

    private final CommentMentionRepository commentMentionRepository;

    public List<MyMentionedCommentResponse> getRecentMentionedComments(Long memberId, boolean inquiryOnly) {
        LocalDateTime since = LocalDateTime.now(KOREA_ZONE_ID).minusDays(LOOKBACK_DAYS);

        return commentMentionRepository.findRecentMentionsForMember(memberId, since).stream()
                .filter(mention -> !inquiryOnly
                        || mention.getComment().getPost().getCategory().isCode(CategoryCode.INQUIRY))
                .limit(MAX_RESULT_SIZE)
                .map(this::toResponse)
                .toList();
    }

    public List<MyMentionedCommentResponse> getRecentMentionedComments(
            String communityCode,
            Long memberId,
            boolean inquiryOnly
    ) {
        LocalDateTime since = LocalDateTime.now(KOREA_ZONE_ID).minusDays(LOOKBACK_DAYS);

        return commentMentionRepository.findRecentMentionsForMember(memberId, since).stream()
                .filter(mention -> mention.getComment()
                        .getPost()
                        .getCategory()
                        .getCountryCommunity()
                        .isCode(communityCode))
                .filter(mention -> !inquiryOnly
                        || mention.getComment().getPost().getCategory().isCode(CategoryCode.INQUIRY))
                .limit(MAX_RESULT_SIZE)
                .map(this::toResponse)
                .toList();
    }

    private MyMentionedCommentResponse toResponse(CommentMention mention) {
        return MyMentionedCommentResponse.of(
                mention.getComment().getPost().getId(),
                mention.getComment().getPost().getTitle(),
                mention.getComment().getPost().getCategory().getName(),
                mention.getComment().getPost().getCategory().getCountryCommunity().getCode(),
                toPreview(mention.getComment().getContent()),
                mention.getCreatedAt()
        );
    }

    private String toPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        String trimmed = content.trim();
        String withoutLeadingMentions = trimmed.replaceFirst("^(?:@[가-힣A-Za-z0-9_]{2,12}\\s*)+", "").trim();

        String previewSource = withoutLeadingMentions.isBlank() ? trimmed : withoutLeadingMentions;

        if (previewSource.length() <= PREVIEW_LENGTH) {
            return previewSource;
        }
        return previewSource.substring(0, PREVIEW_LENGTH) + "...";
    }
}