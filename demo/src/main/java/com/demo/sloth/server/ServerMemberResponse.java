package com.demo.sloth.server;

import java.time.Instant;

public record ServerMemberResponse(Long userId, String displayName, Instant joinedAt) {

    static ServerMemberResponse from(ServerMember member) {
        return new ServerMemberResponse(member.getUser().getId(),
                member.getUser().getDisplayName(), member.getJoinedAt());
    }
}
