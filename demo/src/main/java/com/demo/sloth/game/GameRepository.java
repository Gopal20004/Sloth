package com.demo.sloth.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Page<Game> findAllByOrderByPopularityRankAscNameAsc(Pageable pageable);

    Page<Game> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Game> findBySlug(String slug);
}
