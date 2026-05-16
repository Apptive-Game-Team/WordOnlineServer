# Repository Guidelines

## Project Structure & Module Organization
This repository is a Gradle-based Spring Boot server targeting Java 21. Application code lives under `src/main/java/com/wordonline/server`, organized by feature areas such as `auth`, `session`, `game`, `deck`, `debug`, and `server`. Runtime configuration and seed data live in `src/main/resources` (`application.yml`, `messages*.properties`, `sql/`). Test support files currently live in `src/test/resources`; add Java tests under `src/test/java` using the same package structure as the code under test.

## Build, Test, and Development Commands
- `./gradlew build`: compile, run tests, and assemble the app.
- `./gradlew test`: run the JUnit test suite only.
- `./gradlew bootRun`: start the server locally with Spring Boot.
- `java -jar build/libs/*.jar`: run the packaged application after a build.
- `./deploy.sh` or `./remote-deploy.sh`: deploy using the project scripts.

## Coding Style & Naming Conventions
Use 4-space indentation and keep files in standard Java package layout. Follow existing naming patterns: `*Controller`, `*Service`, `*Repository`, `*Dto`, `*PrefabInitializer`, and `*System`. Classes use `PascalCase`, methods and fields use `camelCase`, and constants use `UPPER_SNAKE_CASE`. Prefer the helper methods already present on domain objects, such as `addComponent(...)` and `addCollider(...)`, instead of mutating internal lists directly. Lombok is used widely; keep annotations consistent with surrounding code.

## Testing Guidelines
The project uses `spring-boot-starter-test`, JUnit Platform, Spring Security test support, Awaitility, and H2 for test data. Put unit and integration tests in `src/test/java`, and name them `*Test` or `*IntegrationTest`. Reuse `src/test/resources/application.yml`, `schema-h2.sql`, and `data-h2.sql` for database-backed tests. Run `./gradlew test` before opening a PR.

## Commit & Pull Request Guidelines
Match the recent commit style: short imperative subjects with an optional scope, for example `refactor(component): use addComponent helper` or `feature(deactivebot)`. Keep commits focused on one concern. PRs should include a clear summary, linked issue or task, test notes, and any API or gameplay impact. For protocol, DTO, or debug-visual changes, include sample payloads or screenshots when helpful.
Name issue branches with the pattern `<issue-label>/<issue-number>`, for example `feature/253`.

## Configuration & Cleanup
Do not commit secrets from `.env` or environment-specific values from `application.yml`. Keep generated files and local artifacts out of git; remove stray files such as `.DS_Store` before committing.
