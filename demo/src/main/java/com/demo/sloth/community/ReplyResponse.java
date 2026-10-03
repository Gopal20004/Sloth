package com.demo.sloth.community;

import java.time.Instant;

public record ReplyResponse(Long id, Long authorId, String authorName,
                            String body, Instant createdAt) {

    static ReplyResponse from(CommunityReply reply) {
        return new ReplyResponse(reply.getId(), reply.getAuthor().getId(),
                reply.getAuthor().getDisplayName(), reply.getBody(), reply.getCreatedAt());
    }
}
