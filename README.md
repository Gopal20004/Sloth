# Sloth

Sloth is a community platform for gamers. Users sign in, browse popular games or search for one, and jump into that game's community to talk with players from around the world. Anyone can create their own server, invite people, and share gameplay videos — both in a game's section and on their own profile.

Think of it as a place where every game has a home, and every player has a voice.

**Project state:** The README roadmap is implemented as a local MVP. The web app and API are available; deployment and scaling are future work.

## What you can do

- **Accounts** — register, log in, manage a profile
- **Game catalog** — curated popular games and search
- **Game communities** — discussion posts and replies for every game
- **Real-time chat** — talk with other players inside a community
- **Servers** — user-created groups with invites and members
- **Videos** — upload gameplay clips to a game's section or your profile

The React web app and backend APIs cover these features. The catalog currently has eight seeded games. A server is a private member list with invites; it does not yet have its own chat or discussion feed.

## How it works

```text
Browser (React + TypeScript)
  | HTTP JSON, multipart uploads, Server-Sent Events
  v
Spring Boot API (authentication, access rules, feature services)
  |                         |
  v                         v
MySQL (users, games,       Local video directory
posts, chat history,       (MP4/WebM files)
servers, clip metadata)
```

The frontend calls `/api` through Vite's proxy in development. Flyway creates the MySQL schema; JPA validates it at startup. Clip metadata is in MySQL while clip bytes are on disk. Redis and Kafka are included in the local stack but no current feature uses them.

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
| Frontend | React, TypeScript, Vite, pnpm, Motion for React |

## Running locally

### Prerequisites

- JDK 21
- Docker Desktop with Compose, running
- Node.js 24 and pnpm 11.25.0 for the web app

### Steps

1. From the repository root, run `docker compose up -d --wait`. This starts MySQL, Redis, and Kafka. MySQL uses a persistent Docker volume.
2. In `demo/`, run `.\mvnw.cmd spring-boot:run` on Windows or `./mvnw spring-boot:run` on macOS/Linux. The normal `DemoApplication` entry point now works with the local database. Flyway creates and seeds the schema at startup.
3. Check `http://localhost:8080/actuator/health`; it should return `"status":"UP"`.
4. In a second terminal, run `cd frontend`, `pnpm install`, then `pnpm dev`. Open `http://localhost:5173`.

The Vite development server proxies `/api` to the backend on port 8080. The backend allows both `http://localhost:5173` and `http://127.0.0.1:5173` by default so either local address can sign in. To build the web app, run `pnpm build` in `frontend/`. For separate hosting, serve `frontend/dist` from a static host, set `VITE_API_BASE_URL` **at build time** to the backend origin, and set backend `SLOTH_ALLOWED_ORIGIN` to the frontend origin. This setting also accepts a comma-separated list of exact origins and replaces the local defaults. This repository does not include production hosting configuration.

The defaults in `compose.yaml` and `application.yaml` are for local development. Copy `.env.example` to `.env` to change the **Compose** passwords. If you change `MYSQL_PASSWORD`, set `DB_PASSWORD` to the same value in the terminal that starts Spring Boot; Compose does not export `.env` values to that terminal. The backend also accepts `DB_URL`, `DB_USER`, `REDIS_HOST`, `REDIS_PORT`, `KAFKA_BOOTSTRAP_SERVERS`, `SLOTH_ALLOWED_ORIGIN`, and `SLOTH_VIDEO_PATH`. The default video path is `./data/videos` relative to the backend working directory, normally `demo/data/videos`; preserve it to keep clips. `docker compose down` stops services while preserving the MySQL volume.

For backend tests, `cd demo` and run `.\mvnw.cmd verify` (Windows) or `./mvnw verify` (macOS/Linux). Testcontainers starts isolated MySQL, Redis, and Kafka containers; Docker must be running, but the Compose stack is not required. For frontend verification, run `pnpm install --frozen-lockfile` and `pnpm build` in `frontend/`. GitHub Actions runs both checks on PRs, plus a backend startup health check.

## Implementation guide: why each part exists

This guide covers the code by component. For each part, it describes the choice, its contribution, and a plausible alternative or limitation. Controllers define routes, request/response records define data sent over the API, services enforce behavior and permissions, and repositories query MySQL. That separation takes more files than a prototype but makes changes easier to locate and test.

### Backend components

| Code | Why it was used and what it adds | Alternative or tradeoff |
|---|---|---|
| `demo/src/main/java/com/demo/sloth/user/` | User, repository, service, and controller provide registration and profiles. A separate public response returns a player's name and join date without exposing email. | Returning the database entity would be shorter but could leak private fields. Richer profiles need new fields, a migration, and privacy rules. |
| `demo/src/main/java/com/demo/sloth/auth/` and `config/SecurityConfig.java` | Spring Security protects writes and private reads. Random bearer tokens are stored as SHA-256 hashes in MySQL sessions; passwords use BCrypt. Logout removes the session. This makes account actions attributable to a player and revocable. | Stateless JWTs avoid a database lookup on each authenticated request, but immediate revocation is harder. The current session approach adds database work per request. |
| `demo/src/main/java/com/demo/sloth/game/` | Stable slugs, a seeded catalog, a curated popularity rank, and repository search let users discover a community immediately. | An activity-based rank and full-text search would grow with the catalog, but need metrics, indexing, and abuse controls. Manual rank needs curation. |
| `demo/src/main/java/com/demo/sloth/community/` | Persisted posts and replies are tied to a game and author, with author-only deletion. Conversations survive refreshes and remain organized by game. | Editing, moderation, reporting, and thread subscriptions would help larger communities but are not implemented. |
| `demo/src/main/java/com/demo/sloth/chat/` | MySQL stores message history; Server-Sent Events deliver new messages after the write commits. Players can reconnect and reload history without constant polling. | The in-memory broadcaster reaches only clients connected to the same backend process. A shared broker would enable multiple instances but needs ordering and delivery handling. |
| `demo/src/main/java/com/demo/sloth/server/` | Membership checks guard private groups. Owners create seven-day random invite codes stored as hashes, can revoke invites, and manage members. This enables private crews without storing readable codes. | One-time or limited-use invites need usage tracking. Public server discovery would change the privacy model. |
| `demo/src/main/java/com/demo/sloth/video/` | Clip metadata is in MySQL; validated MP4/WebM bytes are placed in a configurable directory. Uploads are capped at 100 MB and file cleanup follows transaction completion. This supports game and profile clips without filling the database with media. | Object storage, background transcoding, thumbnails, and a CDN would support multiple instances and more traffic, with more cost and setup. Local files require separate backups. |
| `demo/src/main/java/com/demo/sloth/config/` | Central CORS, security, and handled error responses keep shared HTTP rules out of feature controllers. The web app can call the allowed API origin and receives useful errors. | A gateway could centralize this across many services, but would add an unnecessary component for one backend. |
| `demo/src/main/resources/db/migration/` | Versioned Flyway SQL creates the schema and seeds the games; JPA validates it. New installs and upgrades use the same known database changes. | Automatic schema generation is quick for experiments but gives less control over upgrades and data preservation. |

### Frontend components

| Code | Why it was used and what it adds | Alternative or tradeoff |
|---|---|---|
| `frontend/src/main.tsx` and `App.tsx` | React mounts the app; React Router gives games, profiles, servers, and account pages linkable routes. Protected routes redirect to sign-in. | Server-rendered pages could improve first load and search indexing, but require a server-side hosting layer. |
| `frontend/src/auth.tsx` and `pages/AuthPage.tsx` | One auth context manages registration, sign-in, logout, and current-player state. The bearer token lives in tab-scoped `sessionStorage`, so pages share it and it ends with the tab session. | An HttpOnly cookie would hide the token from JavaScript, but needs cookie and CSRF design. `sessionStorage` is accessible to injected scripts. |
| `frontend/src/api.ts` and `types.ts` | A shared request wrapper attaches tokens, handles common errors, and gives pages typed API responses. This reduces duplicated request code and catches type mistakes in development. | Generated API types could reduce drift from Java response records, but need an API schema and generation step. |
| `frontend/src/pages/HomePage.tsx` | The page loads popular games and debounces and cancels search requests. Discovery responds quickly without sending one request per keystroke. | Full-text search would suit a large catalog; the current name search and first-page display fit the seeded MVP. |
| `frontend/src/pages/GamePage.tsx` | Tabs combine discussion, chat, and clips for one game. Chat loads saved messages, receives SSE events, and deduplicates them. The game feels like one connected community. | Separate routes or a shared event cache may suit larger feature sets, with more navigation and state management. |
| `frontend/src/pages/ServersPage.tsx` | Creation, joining, invite codes, members, and owner controls use the private-server API, so players need no raw API calls. | More granular screens could be easier to extend; current lists fetch only the first 50 records. |
| `frontend/src/pages/ProfilePage.tsx` | Public profile data and clips appear together; owners can edit their name and upload clips. This gives every clip a player home while keeping email private. | Richer social profiles require new fields, privacy settings, and UI. |
| `frontend/src/components.tsx` and `styles.css` | Shared loading/error states, cards, upload controls, and responsive styling keep feature pages consistent on desktop and mobile. | A component library may help a larger team, but adds dependencies and design constraints. |
| `frontend/src/animation.tsx`, `App.tsx`, and `components.tsx` | Motion for React supplies short page entrances, once-per-mount section reveals, staggered game cards, and responsive hover/tap feedback. `LazyMotion` loads the animation feature subset, while `MotionConfig` and `useReducedMotion` respect the device preference. Routes enter immediately so old forms and chat connections are not retained for exit effects. | CSS is enough for simple transitions and costs no extra library bytes. Motion adds dependency and bundle size in exchange for coordinated gestures, viewport triggers, and spring smoothing. Heavy scene transitions or large stagger delays would slow navigation. |
| `frontend/src/ControllerScene.tsx` and `showcase.css` | An original SVG controller sits inside a CSS portal with static lighting, an outlined backdrop, and two layers of spring-smoothed pointer depth. The desktop float pauses outside the viewport, in hidden tabs, or with the scene pause button. Small/touch screens and reduced-motion users get a still scene. Removing backdrop blur keeps moving cards simpler to composite. | This is an illusion of depth, not a navigable 3D world. WebGL would support real cameras, geometry, and dynamic lighting but adds assets, GPU work, context-loss handling, and a fallback. A still illustration would be cheaper again but less interactive. |
| `frontend/src/GameIdentity.tsx` and `frontend/public/game-logos/` | Locally bundled game marks and styled full-title fallbacks replace two-letter placeholders, making communities easier to recognize. [Asset credits](frontend/public/game-logos/README.md) record the source and license. | External image URLs would be easier to change centrally but add runtime network dependencies and potential broken images. |
| `frontend/package.json`, `pnpm-lock.yaml`, and `vite.config.ts` | Locked dependencies and Vite provide repeatable installs, a production build, and a development API proxy. | A server-rendering framework brings more built-in deployment features but adds runtime and setup complexity. |

### Immersive homepage: rendering choices

The visual direction borrows cinematic composition from [Imersion](https://www.imersion.io/) and orbital atmosphere from [Edolus](https://edolus.com/), using original artwork for Sloth. Their media and source code are not bundled.

- **`ControllerScene.tsx`:** Existing Motion values update transforms without putting every pointer movement into React state. A spring softens the controller tilt; the background moves slightly in the opposite direction for depth. Only a visible desktop scene animates continuously. Media-query and page-visibility listeners are cleaned up on unmount, and the pause button uses a stable accessible label with pressed state. These checks save unnecessary work; device class is a useful heuristic, not a guarantee of GPU speed.
- **`showcase.css`:** Static gradients, borders, SVG shading, and a few decorative shapes create the portal, stars, and lighting. Motion changes transforms rather than animating blur, shadows, or layout. Smaller screens omit the stars and shards. These layers still have a paint/compositing cost, but require no downloaded textures, background video, canvas, or additional rendering library.
- **`HomePage.tsx`:** A decorative atmosphere wrapper and quiet horizon labels connect the scene to the page. The normal page scroll, search, and direct community links remain immediately available. All scene decoration is hidden from assistive technology; the pause control is outside that hidden subtree.
- **Alternatives:** A compressed background image could add landscape detail with a fixed download cost. A short video could add cinematic movement but increases transfer, decoding, and battery use. Full 3D is worth considering only for a feature that actually needs camera or object interaction. Adding perpetual particles, large moving blurs, or forced scroll sequences would increase rendering work and make discovery slower.

The production build for this change is approximately **125.64 KB JavaScript + 9.55 KB CSS, gzip**, compared with **125.09 KB + 8.76 KB** before the portal. No dependencies or external scene assets were added. These are build sizes, not an FPS guarantee; representative physical phones still need testing before a public release. Local Chrome checks cover desktop pointer depth, pause/resume, offscreen pause, a static touch layout, reduced motion, search, navigation, and login-page access.

### Local runtime and verification

| Code | Why it was used and what it adds | Alternative or tradeoff |
|---|---|---|
| `compose.yaml` and `.env.example` | One command starts local MySQL, Redis, and Kafka, with configurable development passwords. | Managed services reduce deployment maintenance but add cost and configuration. Redis and Kafka are provisioned, not used by the current features. |
| `demo/src/test/` | API integration tests use Testcontainers, including a real MySQL instance. This catches migration and database behavior that mocks could miss. | Mock-only tests are faster but give less confidence that the full backend starts and persists correctly. |
| `.github/workflows/backend-ci.yml` | PRs run backend tests and start the normal app against Compose services, then check its health. This catches both behavior and startup failures. | More cross-layer browser tests would catch UI/API mismatches, but increase CI time and maintenance. |
| `.github/workflows/frontend-ci.yml` | PRs install the locked dependencies, type-check, and build the web app. This prevents broken TypeScript or bundling from reaching `main`. | A build does not prove browser interactions work; automated UI tests would add that coverage. |

## Account API

API paths below are relative to `http://localhost:8080`. Request and response bodies use JSON except for multipart video uploads and the chat event stream. Private actions require `Authorization: Bearer <token>` from login.

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

`GET /api/games/{slug}/chat/messages` returns recent messages, newest first, with `page` and `size` parameters (`size` 1–100). `GET /api/games/{slug}/chat/stream` is a public Server-Sent Events stream; the `message` event contains each new message. Authenticated players send a message with `POST /api/games/{slug}/chat/messages` (`body`). Clients should load recent messages when connecting or reconnecting to catch anything missed. Live delivery currently works within one backend process; persisted history is available after a reconnect.

## Video API

Authenticated players upload a clip with `POST /api/videos` as multipart form data: `title`, optional `gameSlug`, and `file`. MP4 and WebM files are accepted up to 100 MB. A clip always appears on its owner's profile and appears in a game's section when `gameSlug` is supplied. Anyone can browse `GET /api/users/{userId}/videos` or `GET /api/games/{slug}/videos`, fetch metadata at `GET /api/videos/{id}`, and play the clip from `GET /api/videos/{id}/file`. Owners remove a clip with `DELETE /api/videos/{id}`. Video files are stored under `SLOTH_VIDEO_PATH` (default `demo/data/videos` when run from `demo/`); keep that directory on persistent storage.

## Project structure

```
Sloth/
├── .github/workflows/   Backend and frontend checks
├── compose.yaml         Local infrastructure
├── demo/
│   ├── src/main/java/   Spring Boot API by feature
│   ├── src/main/resources/db/migration/   Database migrations and seed data
│   └── src/test/        Backend integration tests
└── frontend/
    └── src/             React routes, shared components, API client, and styles
```

## Contributing

- Create or select a GitHub issue, then branch from `main` as `feature/<issue>-<short-name>`.
- Submit a pull request referencing the issue. GitHub Actions checks the backend and frontend.
- Do not commit directly to `main`.

## Status

The roadmap MVP is implemented. The next improvements are ordered by the constraints they remove:

1. **Use Redis for shared chat delivery and presence when running multiple backend instances.** Today the SSE broadcaster is in memory, so players connected to different instances will not receive the same live events. Redis Pub/Sub could fan out messages; it would also require reconnection handling because Pub/Sub does not store missed events. MySQL history remains the durable source.
2. **Move clips to shared object storage if the app runs on more than one machine.** Add transcoding, thumbnails, and a CDN as usage grows. Local disk is simpler now, but clips need separate backups and are tied to their host.
3. **Add moderation, reporting, and rate limits before a large public launch.** The current owner controls cover personal content and private servers, not abuse across public discussions, chat, and clips.
4. **Improve discovery and long lists.** Measure activity for ranking, add catalog management or full-text search, and expose pagination throughout the web app. Curated rank and first-page screens are predictable for eight games but will become limiting.
5. **Add browser-level tests for account, discussion, chat, invite, and upload flows.** Backend integration tests and the frontend build cover important parts separately, but do not automatically verify the entire player journey.

Kafka could support durable asynchronous work such as clip processing or analytics later. Connecting it to the current request path without a consumer would increase operational complexity without adding a user-facing feature.
