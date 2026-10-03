# Sloth

Sloth is a community platform for gamers. Users sign in, browse popular games or search for one, and jump into that game's community to talk with players from around the world. Anyone can create their own server, invite people, and share gameplay videos — both in a game's section and on their own profile.

Think of it as a place where every game has a home, and every player has a voice.

## Feature roadmap

- **Accounts** — register, log in, manage a profile (backend API available)
- **Game catalog** — curated popular games and search (backend API available)
- **Game communities** — discussion posts and replies for every game (backend API available)
- **Real-time chat** — talk with other players inside a community
- **Servers** — user-created groups with invites and members
- **Videos** — upload gameplay clips to a game's section or your profile

Account, game catalog, and community discussion backend APIs are available. The other features remain planned.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Actuator) |
| Database | MySQL, migrations with Flyway |
| Cache / presence | Redis |
| Messaging / events | Apache Kafka |
| Build | Maven (wrapper included) |
| Local infra | Docker via Testcontainers |
| Frontend | TBD |

## Running locally

### Prerequisites

- JDK 21
- Docker Desktop (must be running — MySQL, Redis and Kafka are started automatically in containers)

### Steps

1. Clone the repo and open `demo/` in IntelliJ (or open the root and let it detect `demo/pom.xml` as a Maven project).
2. Make sure Docker Desktop is running.
3. Run `TestDemoApplication` (in `src/test/java/com/demo/sloth`). This starts the app together with MySQL, Redis and Kafka containers.
   - From the terminal instead: `cd demo` then `.\mvnw.cmd spring-boot:test-run` (Windows) or `./mvnw spring-boot:test-run` (macOS/Linux).
4. The app listens on `http://localhost:8080`. Check it's alive at `http://localhost:8080/actuator/health` — you should see `"status":"UP"`.

The first run pulls container images (~1 GB) and takes a minute or two; later runs take about 15 seconds.

> `DemoApplication` (in `src/main`) is the real entry point but has no database configured yet, so it will fail to start until a proper `application.yaml` for a real environment is added.

## Account API

All request and response bodies use JSON. Start the backend as described above.

| Action | Method and path | Body or header |
|---|---|---|
| Register | `POST /api/users/register` | `displayName`, `email`, `password` |
| Log in | `POST /api/auth/login` | `email`, `password` |
| View profile | `GET /api/users/me` | `Authorization: Bearer <token>` |
| Update display name | `PATCH /api/users/me` | Bearer token and `displayName` |
| Log out | `DELETE /api/auth/logout` | Bearer token |

Login returns an opaque token and its expiration time. Store the token securely; it expires after 30 days, and logout revokes it. Password hashes and token hashes are stored in MySQL. Registration accepts an email address only once, regardless of letter case.

## Game catalog API

The game catalog is public. `GET /api/games/popular` returns the manually curated order, `GET /api/games/search?q=minecraft` searches names without regard to letter case, and `GET /api/games/{slug}` returns one game. List endpoints accept `page` (starting at 0) and `size` (1–50), and return `content`, `page`, `size`, `totalElements`, and `totalPages`. The initial catalog is seeded by Flyway; popularity order is not a live ranking.

## Community API

Anyone can read `GET /api/games/{slug}/posts`, `GET /api/posts/{id}`, and `GET /api/posts/{id}/replies`. A bearer token is required to create a post with `POST /api/games/{slug}/posts` (`title`, `body`) or a reply with `POST /api/posts/{id}/replies` (`body`). Authors can remove their own content with `DELETE /api/posts/{id}` and `DELETE /api/replies/{id}`. List endpoints use the same `page` and `size` parameters as the catalog.

## Project structure

```
Sloth/
└── demo/     Spring Boot backend
```

A frontend will be added alongside it later.

## Contributing

- all work happens on a branch off main, named feature/<issue>-<short-name>
- every change goes through a pull request that references its issue
- main is never committed to directly

## Status

Early development — started September 2026.
