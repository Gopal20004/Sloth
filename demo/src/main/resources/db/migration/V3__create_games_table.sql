CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    cover_url VARCHAR(500),
    popularity_rank INT NOT NULL
);

CREATE INDEX idx_games_popularity ON games(popularity_rank, name);

INSERT INTO games (slug, name, description, popularity_rank) VALUES
    ('minecraft', 'Minecraft', 'Build, explore, and survive in a block-based world.', 1),
    ('fortnite', 'Fortnite', 'Play battle royale matches and creative experiences.', 2),
    ('roblox', 'Roblox', 'Explore and create experiences with a global community.', 3),
    ('valorant', 'VALORANT', 'Compete in team-based tactical matches.', 4),
    ('league-of-legends', 'League of Legends', 'Play team-based strategy matches.', 5),
    ('counter-strike-2', 'Counter-Strike 2', 'Compete in tactical first-person matches.', 6),
    ('elden-ring', 'Elden Ring', 'Explore a vast fantasy world and challenging battles.', 7),
    ('grand-theft-auto-v', 'Grand Theft Auto V', 'Explore an open world and online multiplayer.', 8);
