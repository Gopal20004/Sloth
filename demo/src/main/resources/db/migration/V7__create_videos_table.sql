CREATE TABLE videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    game_id BIGINT,
    title VARCHAR(160) NOT NULL,
    storage_key VARCHAR(50) NOT NULL UNIQUE,
    content_type VARCHAR(20) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_videos_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_videos_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE SET NULL
);

CREATE INDEX idx_videos_owner_created ON videos(owner_id, created_at);
CREATE INDEX idx_videos_game_created ON videos(game_id, created_at);
