package com.demo.sloth.server;

import java.time.Instant;

public record InviteResponse(Long id, String code, Instant expiresAt) {
}
