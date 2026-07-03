package com.gahih.domain.comment.dto;

import com.gahih.domain.comment.entity.Comment;
import com.gahih.domain.reaction.enumtype.ReactionType;
import lombok.Getter;

@Getter
public class CommentReactionResult {

    private final Long commentId;
    private final Long likeCount;
    private final Long dislikeCount;
    private final ReactionType currentReactionType;

    public CommentReactionResult(
            Long commentId,
            Long likeCount,
            Long dislikeCount,
            ReactionType currentReactionType
    ) {
        this.commentId = commentId;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.currentReactionType = currentReactionType;
    }

    public static CommentReactionResult of(Comment comment, ReactionType currentReactionType) {
        return new CommentReactionResult(
                comment.getId(),
                comment.getLikeCount(),
                comment.getDislikeCount(),
                currentReactionType
        );
    }

    public boolean isLiked() {
        return currentReactionType == ReactionType.LIKE;
    }

    public boolean isDisliked() {
        return currentReactionType == ReactionType.DISLIKE;
    }

    public boolean isReacted() {
        return currentReactionType != null;
    }
}