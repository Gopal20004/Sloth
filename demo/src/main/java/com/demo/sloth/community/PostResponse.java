package com.demo.sloth.community;

import java.time.Instant;

public record PostResponse(Long id, String gameSlug, Long authorId, String authorName,
                           String title, String body, Instant createdAt) {

    static PostResponse from(CommunityPost post) {
        return new PostResponse(post.getId(), post.getGame().getSlug(),
                post.getAuthor().getId(), post.getAuthor().getDisplayName(),
                post.getTitle(), post.getBody(), post.getCreatedAt());
    }
}
