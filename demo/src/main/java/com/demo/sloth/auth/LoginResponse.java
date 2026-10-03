package com.demo.sloth.auth;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
