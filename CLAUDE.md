# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build               # Build with tests
./gradlew clean build -x test # Build without tests

# Test
./gradlew test                # Run all tests

# Run
java -jar build/libs/word-online-server-0.0.1.jar

# Deploy
./deploy.sh                   # Local server deployment
./remote-deploy.sh            # Remote server deployment
```

Required environment variables: `PORT`, `EXTERNAL_PORT`, `PROTOCOL`, `DOMAIN`, `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PW`, `DISCORD_WEBHOOK_URL`, `JWT_PUBLIC_KEY`, `ACCOUNT_SERVER_URL`.

Tests use H2 in-memory DB and do not require external services.

## Architecture

**WordOnlineServer** is a Spring Boot (Java 21) multiplayer word game backend using WebSocket for real-time communication, PostgreSQL for persistence, and JWT + OAuth2 for authentication.

### Component-Based Game Object System

The core game logic uses a Unity-like component pattern (see `docs/component-structure.md`):

- `GameObject` — base entity with a `Master` (LeftPlayer/RightPlayer/None), `Position` (Vector3), `Status`, and a list of `Component`s
- `Component` — abstract base with `start()` / `update()` / `onDestroy()` lifecycle
- `PrefabInitializer` — assembles GameObjects from `PrefabType` definitions
- Common components: `RigidBody`, `CircleCollider`, `Mob`, `SelfAttacker`

### Game Loop & Session Flow

```
WebSocket input → InputController
  → SessionService → SessionObject → GameContext
  → MagicInputHandler
  → GameLoop / WordOnlineLoop (per-frame tick)
  → response broadcast to /game/{sessionId}/frameInfos/{userId}
```

- `GameLoop` / `WordOnlineLoop` — core tick system for PvP
- `PveLoop` — PvE game mode
- `SessionService` — manages active sessions

### Magic (Spell) System

Magics are card-based spells parsed from the database via `DatabaseMagicParser`. Types: Spawn, Shoot, Explode, Build, Drop. The `ElementalChart` governs type-advantage interactions.

### Physics

`Physics` interface / `SimplePhysics` implementation — provides `overlapSphereAll()`, `raycast()`, and `overlapBoxAll()` spatial queries used by components for collision and targeting.

### Key Packages

| Package | Responsibility |
|---------|---------------|
| `game/domain/object/` | GameObject, Component, PrefabInitializer |
| `game/domain/magic/` | Magic definitions, parsing, elemental chart |
| `game/` | GameLoop, SessionService, InputController |
| `auth/` | JWT provider, Spring Security config, OAuth2 |
| `config/` | Localization, Discord webhook logging appender |
| `debug/` | Admin HTTP endpoints for summoning magics/prefabs |

### Localization

Message bundles are in `src/main/resources/` (`messages.properties` = Korean default, `messages_en.properties`, `messages_ko.properties`). Language is resolved from the `Accept-Language` header.

### CI/CD

GitHub Actions (`.github/workflows/docker-ci.yml`) builds a Docker image and pushes to GHCR. Pushes to `main` produce the `dev` tag; pushes to `deploy` produce `latest`.
