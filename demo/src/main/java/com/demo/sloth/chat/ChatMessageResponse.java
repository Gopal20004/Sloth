package com.demo.sloth.chat;

import java.time.Instant;

public record ChatMessageResponse(Long id, String gameSlug, Long senderId, String senderName,
                                  String body, Instant sentAt) {

    static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getGame().getSlug(),
                message.getSender().getId(), message.getSender().getDisplayName(),
                message.getBody(), message.getSentAt());
    }
}
