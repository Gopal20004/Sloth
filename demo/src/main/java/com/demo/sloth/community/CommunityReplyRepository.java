package com.demo.sloth.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityReplyRepository extends JpaRepository<CommunityReply, Long> {
    Page<CommunityReply> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);
}
