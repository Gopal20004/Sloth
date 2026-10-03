package com.demo.sloth.game;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {

    private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<GameResponse> popular(int page, int size) {
        return PageResponse.from(repository.findAllByOrderByPopularityRankAscNameAsc(
                PageRequest.of(page, size)).map(GameResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<GameResponse> search(String query, int page, int size) {
        return PageResponse.from(repository.findByNameContainingIgnoreCase(query.trim(),
                PageRequest.of(page, size)).map(GameResponse::from));
    }

    @Transactional(readOnly = true)
    public GameResponse getBySlug(String slug) {
        return repository.findBySlug(slug).map(GameResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }
}
