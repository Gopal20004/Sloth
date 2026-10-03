# Sloth

Sloth is a community platform for gamers. Users sign in, browse popular games or search for one, and jump into that game's community to talk with players from around the world. Anyone can create their own server, invite people, and share gameplay videos — both in a game's section and on their own profile.

Think of it as a place where every game has a home, and every player has a voice.

## Feature roadmap

- **Accounts** — register, log in, manage a profile
- **Game catalog** — curated popular games and search
- **Game communities** — discussion posts and replies for every game
- **Real-time chat** — talk with other players inside a community
- **Servers** — user-created groups with invites and members
- **Videos** — upload gameplay clips to a game's section or your profile

The React web app and backend APIs cover the roadmap features.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Actuator) |
| Database | MySQL, migrations with Flyway |
| Cache / presence | Redis (provisioned for future use) |
| Messaging / events | Apache Kafka (provisioned for future use) |
| Build | Maven (wrapper included) |
| Local infra | Docker Compose; Testcontainers for integration tests |
| Frontend | React, TypeScript, Vite, pnpm |

## Running locally

### Prerequisites

- JDK 21
- Docker Desktop with Compose
- Node.js 24 and pnpm 11.25.0 for the web app

### Steps

1. From the repository root, run `docker compose up -d`. This starts MySQL, Redis, and Kafka. MySQL uses a persistent Docker volume.
2. In `demo/`, run `.\mvnw.cmd spring-boot:run` on Windows or `./mvnw spring-boot:run` on macOS/Linux. The normal `DemoApplication` entry point now works with the local database. Flyway creates and seeds the schema at startup.
3. Check `http://localhost:8080/actuator/health`; it should return `"status":"UP"`.
4. In a second terminal, run `cd frontend`, `pnpm install`, then `pnpm dev`. Open `http://localhost:5173`.

The Vite development server proxies `/api` to the backend on port 8080. To build the web app, run `pnpm build` in `frontend/`. For a separately hosted build, set `VITE_API_BASE_URL` to the backend origin and allow that origin with `SLOTH_ALLOWED_ORIGIN` in the backend environment.

The defaults in `compose.yaml` and `application.yaml` are for local development. Copy `.env.example` to `.env` if you want to change the Compose passwords, and set `DB_PASSWORD` to the same MySQL user password before starting the backend. `DB_URL`, `DB_USER`, `REDIS_HOST`, `REDIS_PORT`, `KAFKA_BOOTSTRAP_SERVERS`, `SLOTH_ALLOWED_ORIGIN`, and `SLOTH_VIDEO_PATH` can also be set through environment variables. Keep `demo/data/videos` if you want uploaded clips to survive restarts.

For backend tests, `cd demo` and run `.\mvnw.cmd verify` (Windows) or `./mvnw verify` (macOS/Linux). Testcontainers starts isolated MySQL, Redis, and Kafka containers; the Compose stack is not required for tests.

## Account API

All request and response bodies use JSON. Start the backend as described above.

| Action | Method and path | Body or header |
|---|---|---|
| Register | `POST /api/users/register` | `displayName`, `email`, `password` |
| Log in | `POST /api/auth/login` | `email`, `password` |
| View profile | `GET /api/users/me` | `Authorization: Bearer <token>` |
| View public player profile | `GET /api/users/public/{id}` | No token; returns name and join date only |
| Update display name | `PATCH /api/users/me` | Bearer token and `displayName` |
| Log out | `DELETE /api/auth/logout` | Bearer token |

Login returns an opaque token and its expiration time. Store the token securely; it expires after 30 days, and logout revokes it. Password hashes and token hashes are stored in MySQL. Registration accepts an email address only once, regardless of letter case.

## Game catalog API

The game catalog is public. `GET /api/games/popular` returns the manually curated order, `GET /api/games/search?q=minecraft` searches names without regard to letter case, and `GET /api/games/{slug}` returns one game. List endpoints accept `page` (starting at 0) and `size` (1–50), and return `content`, `page`, `size`, `totalElements`, and `totalPages`. The initial catalog is seeded by Flyway; popularity order is not a live ranking.

## Community API

Anyone can read `GET /api/games/{slug}/posts`, `GET /api/posts/{id}`, and `GET /api/posts/{id}/replies`. A bearer token is required to create a post with `POST /api/games/{slug}/posts` (`title`, `body`) or a reply with `POST /api/posts/{id}/replies` (`body`). Authors can remove their own content with `DELETE /api/posts/{id}` and `DELETE /api/replies/{id}`. List endpoints use the same `page` and `size` parameters as the catalog.

## Server API

Server endpoints require a bearer token. `POST /api/servers` (`name`, optional `description`) creates a private group. `GET /api/servers/mine` lists your groups, and members can view `GET /api/servers/{id}` and `GET /api/servers/{id}/members`. Owners generate seven-day invite codes with `POST /api/servers/{id}/invites` and revoke them with `DELETE /api/servers/{id}/invites/{inviteId}`. Share the returned code with a player, who joins with `POST /api/servers/join` (`code`). A member leaves with `DELETE /api/servers/{id}/members/me`; owners remove members with `DELETE /api/servers/{id}/members/{userId}` or delete the group with `DELETE /api/servers/{id}`. Invite codes are stored only as hashes and shown once when created.

## Chat API

`GET /api/games/{slug}/chat/messages` returns recent messages, newest first, with `page` and `size` parameters. `GET /api/games/{slug}/chat/stream` is a public Server-Sent Events stream; the `message` event contains each new message. Authenticated players send a message with `POST /api/games/{slug}/chat/messages` (`body`). Clients should load recent messages when connecting or reconnecting to catch anything missed. Live delivery currently works within one backend process; persisted history is available after a reconnect.

## Video API

Authenticated players upload a clip with `POST /api/videos` as multipart form data: `title`, optional `gameSlug`, and `file`. MP4 and WebM files are accepted up to 100 MB. A clip always appears on its owner's profile and appears in a game's section when `gameSlug` is supplied. Anyone can browse `GET /api/users/{userId}/videos` or `GET /api/games/{slug}/videos`, fetch metadata at `GET /api/videos/{id}`, and play the clip from `GET /api/videos/{id}/file`. Owners remove a clip with `DELETE /api/videos/{id}`. Video files are stored under `SLOTH_VIDEO_PATH` (default `demo/data/videos` when run from `demo/`); keep that directory on persistent storage.

## Project structure

```
Sloth/
├── demo/       Spring Boot backend
└── frontend/   React web app
```

## Contributing

- all work happens on a branch off main, named feature/<issue>-<short-name>
- every change goes through a pull request that references its issue
- main is never committed to directly

## Status

Roadmap MVP implemented. Chat events currently broadcast within one backend process, the game catalog has a curated popularity order, and video files use local persistent storage.
