package com.gahih.domain.post.dto;

import com.gahih.domain.post.entity.Post;
import com.gahih.domain.reaction.enumtype.ReactionType;
import lombok.Getter;

@Getter
public class PostReactionResult {

    private final Long postId;
    private final Long likeCount;
    private final Long dislikeCount;
    private final ReactionType currentReactionType;

    public PostReactionResult(
            Long postId,
            Long likeCount,
            Long dislikeCount,
            ReactionType currentReactionType
    ) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.currentReactionType = currentReactionType;
    }

    public static PostReactionResult of(Post post, ReactionType currentReactionType) {
        return new PostReactionResult(
                post.getId(),
                post.getLikeCount(),
                post.getDislikeCount(),
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