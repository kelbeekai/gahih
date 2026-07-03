package com.gahih.domain.post.dto;

import lombok.Getter;

@Getter
public class PostNavigationResponse {

    private final PostNavigationItemResponse previousPost;
    private final PostNavigationItemResponse nextPost;

    private PostNavigationResponse(
            PostNavigationItemResponse previousPost,
            PostNavigationItemResponse nextPost
    ) {
        this.previousPost = previousPost;
        this.nextPost = nextPost;
    }

    public static PostNavigationResponse of(
            PostNavigationItemResponse previousPost,
            PostNavigationItemResponse nextPost
    ) {
        return new PostNavigationResponse(previousPost, nextPost);
    }

    public static PostNavigationResponse empty() {
        return new PostNavigationResponse(null, null);
    }

    public boolean hasPrevious() {
        return previousPost != null;
    }

    public boolean hasNext() {
        return nextPost != null;
    }
}