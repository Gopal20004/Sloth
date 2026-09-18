# Sloth

Sloth is a community platform for gamers. Users sign in, browse popular games or search for one, and jump into that game's community to talk with players from around the world. Anyone can create their own server, invite people, and share gameplay videos — both in a game's section and on their own profile.

Think of it as a place where every game has a home, and every player has a voice.

## Features (planned)

- **Accounts** — register, log in, manage a profile
- **Game catalog** — popular games on the home page, plus search
- **Game communities** — a discussion space for every game
- **Real-time chat** — talk with other players inside a community
- **Servers** — user-created groups with invites and members
- **Videos** — upload gameplay clips to a game's section or your profile

Nothing is built yet — this list is the roadmap.

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
