package com.demo.sloth.server;

import java.time.Instant;

public record ServerResponse(Long id, String name, String description, Long ownerId, Instant createdAt) {

    static ServerResponse from(PlayerServer server) {
        return new ServerResponse(server.getId(), server.getName(), server.getDescription(),
                server.getOwner().getId(), server.getCreatedAt());
    }
}
