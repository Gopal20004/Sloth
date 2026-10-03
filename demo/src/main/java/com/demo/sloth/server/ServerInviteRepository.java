package com.demo.sloth.server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface ServerInviteRepository extends JpaRepository<ServerInvite, Long> {
    Optional<ServerInvite> findByCodeHashAndExpiresAtAfter(String codeHash, Instant now);
}
