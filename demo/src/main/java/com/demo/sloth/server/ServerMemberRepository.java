package com.demo.sloth.server;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerMemberRepository extends JpaRepository<ServerMember, Long> {
    boolean existsByServerIdAndUserId(Long serverId, Long userId);
    Optional<ServerMember> findByServerIdAndUserId(Long serverId, Long userId);
    Page<ServerMember> findByUserIdOrderByJoinedAtDesc(Long userId, Pageable pageable);
    Page<ServerMember> findByServerIdOrderByJoinedAtAsc(Long serverId, Pageable pageable);
}
