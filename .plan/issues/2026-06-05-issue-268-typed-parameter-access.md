# 2026-06-05 — Refactor parameter access to typed keys

- Date: 2026-06-05
- GitHub Issue: #268
- Status: Implemented

## Goal

Replace scattered raw string parameter lookups with a typed, object-oriented access layer that keeps the existing database schema and lookup behavior intact.

The first target shape is:
- typed `GameObjectKey` values for DB object names such as `zap_mouse`
- typed `ParameterKey` values for parameter names such as `hp`
- an object-scoped wrapper retrieved once per method/block, such as `var zapMouseParameters = parameters.object(GameObjectKey.ZAP_MOUSE)`, then `zapMouseParameters.intValue(ParameterKey.HP)`
- compatibility for existing `parameters.getValue(String, String)` during incremental migration

Typed keys are thin constants over existing DB string names. They are not a new domain registry and do not enforce object-parameter validity yet.

## Non-goals

- No database schema changes.
- No schema or behavior rewrite while migrating prefab initializer parameter calls.
- No generated parameter model per prefab yet.
- No change to balancing data or SQL values.

## Context / Constraints

- Current code called `parameters.getValue("object_name", "parameter_name")` across many prefabs, services, and bot logic.
- Raw strings allow typos to compile and fail only at runtime.
- Parameter data remains dynamic and DB-backed through `parameter_values`.
- Existing cache invalidation and `ParameterRepository` behavior should remain compatible.
- Worktree may contain unrelated user files from earlier parameter experiments; do not stage or modify them unless explicitly included.
- Existing empty files under `src/main/java/com/wordonline/server/game/domain/parameter` must be inspected before editing. Adopt them only if intentionally using those filenames; otherwise leave them alone.
- `GameObjectKey` must store canonical DB names like `zap_mouse`; do not infer names from enum constants.
- Missing object/parameter behavior must stay the same as today: the existing service throws `IllegalArgumentException("Parameter not found: ...")`.
- Numeric conversion must match current call-site behavior:
  - `doubleValue(...)` returns the DB-backed `double`.
  - `intValue(...)` uses Java's `(int)` cast semantics, including truncation.
  - `longValue(...)` uses Java's `(long)` cast semantics.
  - `floatValue(...)` uses Java's `(float)` cast semantics.

## Approach (Checklist)
- [x] **Step 0: Recon** (Inspect existing code, locate files)
  - Count high-frequency object keys and parameter keys.
  - Inspect `Parameters`, `ParameterService`, `ParameterRepository`, and existing tests.
  - Identify narrow migration targets with different conversion needs. Prefer `GameTimer` (`long`) plus `ZapMousePrefabInitializer` (`int` and `float`).
- [x] **Step 1: Implementation** (Code changes, file paths)
  - Add typed key enums under `src/main/java/com/wordonline/server/game/domain/parameter`.
  - Add object-scoped parameter accessor wrapper.
  - Add typed overloads/factory methods to `Parameters`.
  - Wrapper must delegate through the existing `Parameters.getValue(String, String)` path so cache/query behavior stays identical.
  - Keep raw string `getValue(...)` public and non-deprecated for now to avoid noisy warnings across 363 existing callers.
  - Migrate all string-literal game object lookups to local `GameObjectParameters` wrapper variables before reading values.
  - Keep dynamic runtime lookups, such as card-name based lookups, on the existing raw string API.
- [x] **Step 2: Tests** (Unit tests, manual verification steps)
  - Add focused tests for typed lookup, numeric conversion cast behavior, caching/delegation compatibility, and missing parameter errors.
  - Add at least one compatibility test proving raw and typed calls return the same value for the same parameter.
  - Run `./gradlew test` or at least targeted parameter tests.
- [x] **Step 3: Rollout / Rollback** (Feature flags, migration steps)
  - Roll out by migrating call sites incrementally in future PRs.
  - Roll back by reverting typed facade changes; DB and existing string API stay unchanged.

## Validation
- **Commands to run:**
  - `./gradlew test --tests com.wordonline.server.game.service.GameObjectParametersTest`
  - `./gradlew test`
- **Expected output:**
  - Tests pass.
  - Existing dynamic string callers still compile.
  - Migrated typed callers compile through local `GameObjectParameters` variables without string literals for object/parameter keys.
  - Typed calls preserve missing-key exception behavior and Java cast behavior.

## Risks & Rollback
- **Risks:**
  - Large enum lists can drift from DB seed data if new parameters are added without updating code.
  - Numeric conversion helpers can hide precision loss if not named clearly.
  - Migrating too many call sites in one PR increases review risk.
- **Rollback steps:** Revert the typed facade commit. Existing DB schema and raw string API remain available.

## Open Questions
- Should missing enum keys be added manually with each SQL change, or should a future validation test compare DB seed keys against enum constants?
- Rejected feedback: do not deprecate `getValue(String, String)` in this PR. With 363 callers, warnings would be noisy and would not improve safety until migration policy exists.
- Multi-model plan review: fast and medium feedback applied; heavy review passed with no blockers.
- User expanded implementation scope after review: all string-literal game object lookups migrated through object-scoped parameter wrapper variables; dynamic runtime key lookups intentionally left unchanged.
