# Repository Guidelines

## Project Structure & Module Organization
This repository is a Gradle-based Spring Boot server targeting Java 21. Application code lives under `src/main/java/com/wordonline/server`, organized by feature areas such as `auth`, `session`, `game`, `deck`, `debug`, and `server`. Runtime configuration lives in `src/main/resources`. Test support files currently live in `src/test/resources`; add Java tests under `src/test/java` using the same package structure as the code under test.
For cross-repo context, see [related-repositories.md](air-file://g9ubn80st39rtru4viru/Users/jeong-yunseong/development/word-online/dev/game-server/docs/related-repositories.md?type=file&root=%252F).

## Build, Test, and Development Commands
- `./gradlew build`: compile, run tests, and assemble the app.
- `./gradlew test`: run the JUnit test suite only.
- `./gradlew bootRun`: start the server locally with Spring Boot.
- `java -jar build/libs/*.jar`: run the packaged application after a build.
- `./deploy.sh` or `./remote-deploy.sh`: deploy using the project scripts.

## Coding Style & Naming Conventions
Use 4-space indentation and keep files in standard Java package layout. Follow existing naming patterns: `*Controller`, `*Service`, `*Repository`, `*Dto`, `*PrefabInitializer`, and `*System`. Classes use `PascalCase`, methods and fields use `camelCase`, and constants use `UPPER_SNAKE_CASE`. Prefer the helper methods already present on domain objects, such as `addComponent(...)` and `addCollider(...)`, instead of mutating internal lists directly. Lombok is used widely; keep annotations consistent with surrounding code.

### Configuration values

Do not inject individual settings with `@Value`. Group related settings into one `@ConfigurationProperties` class and constructor-inject that object, so the values are type-safe, discoverable in one place, and injectable into tests without reflection.

```java
@ConfigurationProperties(prefix = "server")
public record ServerIdentityProperties(String protocol, String domain, Integer externalPort) {}
```

Register properties classes with `@ConfigurationPropertiesScan` on the application class. Define each default in one place. Annotation arguments that require a string literal, such as `@Scheduled(fixedDelayString = "${...}")`, are the only exception. Existing `@Value` usage inside the scope you are already changing moves to a properties object; do not bulk-refactor beyond that scope.

## Testing Guidelines
The project uses `spring-boot-starter-test`, JUnit Platform, Spring Security test support, Awaitility, and H2 for test data. Put unit and integration tests in `src/test/java`, and name them `*Test` or `*IntegrationTest`. Reuse `src/test/resources/application.yml`, `schema-h2.sql`, and `data-h2.sql` for database-backed tests. Run `./gradlew test` before opening a PR.

## Commit & Pull Request Guidelines
Match the recent commit style: short imperative subjects with an optional scope, for example `refactor(component): use addComponent helper` or `feature(deactivebot)`. Keep commits focused on one concern. PRs should include a clear summary, linked issue or task, test notes, and any API or gameplay impact. For protocol, DTO, or debug-visual changes, include sample payloads or screenshots when helpful.
Name issue branches with the pattern `<issue-label>/<issue-number>`, for example `feature/253`.

Every issue and pull request must set an assignee and a label. Do not leave either blank.

- Assignee: `--assignee @me`.
- Label: use the same value as the branch prefix, so branch `feature/253` carries label `feature`. Check the available labels with `gh label list`; this repo has `feature`, `fix`, `refactor`, `documentation`, and `bug`. Do not invent new labels — ask when none of them fit.
- Do not attach a project.

```bash
gh issue create --title "..." --body "..." --assignee @me --label feature
gh pr create --base <base> --title "..." --body "..." --assignee @me --label feature
```

Confirm both landed with `gh issue view <n> --json assignees,labels` after creating.

## Configuration & Cleanup
Do not commit secrets from `.env` or environment-specific values from `application.yml`. Keep generated files and local artifacts out of git; remove stray files such as `.DS_Store` before committing.

## Database Changes

`../database/migration` is the source of truth for the shared game database.
Do not add production schema, seed, backfill, or gameplay data SQL under
`src/main/resources`. Add a new versioned Flyway migration in the database
repository and publish that commit before publishing game-server code that
depends on it. Test-only H2 fixtures remain under `src/test/resources`.

## Architecture & Game Engine Reference

For in-depth explanations of the server systems, refer to the following developer documentation:
- **System Overview & Protocols**: [system-overview.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/system-overview.md)
- **Thread & Session Management**: [session-management.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/session-management.md)
- **Game Loop Tick & Systems Order**: [game-loop.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/game-loop.md)
- **GameObjects & Component Lifecycle**: [objects-components.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/objects-components.md)
- **Prefab Initialization System**: [prefab-system.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/prefab-system.md)
- **Physics, Collisions & Movement**: [physics-system.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/physics-system.md)
- **Magic Spell Recipes & Input Flow**: [magic-system.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/magic-system.md)
- **Database Schema & Parameters**: [database-schema.md](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/database-schema.md)
