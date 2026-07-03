package com.gahih.domain.post.dto;

import lombok.Getter;

@Getter
public class PostNavigationItemResponse {

    private final Long postId;
    private final int page;
    private final String title;

    private PostNavigationItemResponse(Long postId, int page, String title) {
        this.postId = postId;
        this.page = page;
        this.title = title;
    }

    public static PostNavigationItemResponse of(Long postId, int page, String title) {
        return new PostNavigationItemResponse(postId, page, title);
    }
}