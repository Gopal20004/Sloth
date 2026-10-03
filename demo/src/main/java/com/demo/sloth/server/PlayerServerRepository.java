package com.demo.sloth.server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerServerRepository extends JpaRepository<PlayerServer, Long> {
}
