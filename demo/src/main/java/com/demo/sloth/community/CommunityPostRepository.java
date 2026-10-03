package com.demo.sloth.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    Page<CommunityPost> findByGameSlugOrderByCreatedAtDesc(String gameSlug, Pageable pageable);
}
