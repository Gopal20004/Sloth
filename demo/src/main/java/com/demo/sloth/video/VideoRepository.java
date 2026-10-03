package com.demo.sloth.video;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findByGameSlugOrderByCreatedAtDesc(String gameSlug, Pageable pageable);
    Page<Video> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);
}
