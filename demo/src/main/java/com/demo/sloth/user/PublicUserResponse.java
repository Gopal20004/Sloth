package com.demo.sloth.user;

import java.time.Instant;

public record PublicUserResponse(Long id, String displayName, Instant createdAt) {
}
