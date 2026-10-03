package com.demo.sloth.game;

public record GameResponse(Long id, String slug, String name, String description, String coverUrl) {

    static GameResponse from(Game game) {
        return new GameResponse(game.getId(), game.getSlug(), game.getName(),
                game.getDescription(), game.getCoverUrl());
    }
}
