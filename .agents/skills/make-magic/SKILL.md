---
name: make-magic
description: Scaffold a new game magic in this repository when the user asks to create or add a magic. Use for requests like "make magic", "add a new magic", or "scaffold a drop/build/shoot magic" that need a Magic class and SQL registration. Pair it with `make-prefab` when the magic also needs a new prefab or prefab-specific runtime components.
---

# Make Magic

Use this skill when adding a new magic to `word-online/dev/game-server`.

## Scope

This skill covers the minimum scaffold:

- create the `Magic` class under `src/main/java/com/wordonline/server/game/domain/magic/implement/...`
- prepare one SQL file per magic under `src/main/resources/sql/magic/` for `magics` and `magic_cards`
- use an existing prefab bean or coordinate with `make-prefab` when a new prefab is required

## Workflow

1. Inspect a nearby magic of the same family first.
2. Keep Spring bean names consistent:
   `@Component("magic_name")` for magic classes.
3. Choose the correct base class before writing code:
   `AbstractDropMagic`, `AbstractShotMagic`, `AbstractExplosionMagic`, `AbstractSummonMagic`, or a direct `Magic` subclass when needed.
4. Point the magic at an existing prefab when possible. If the task needs a new prefab, use `make-prefab` alongside this skill instead of adding prefab wiring here.
5. Write a dedicated SQL file at `src/main/resources/sql/magic/<magic_name>.sql`. Use the magic bean name as `magics.name`, and use card names exactly as stored in `cards.name` with leading capitals such as `Nature`, `Wind`, and `Drop`.
6. For SQL examples and reusable templates, read `references/magic-sql.md`.
7. Verify compilation with `./gradlew compileJava` when possible.

## File Patterns

- Drop magic:
  `domain/magic/implement/drop/<Name>Magic.java`
- Shoot magic:
  `domain/magic/implement/shoot/<Name>Magic.java`

## Constraints

- Match existing naming conventions exactly: `LeafairMagic`.
- Reuse nearby parameter keys when possible. Do not introduce new DB-backed parameter names unless the task explicitly includes data changes.
- `magics.name` must match the Spring magic bean name exactly, for example `leafair`.
- `cards.name` uses leading-capital card names, for example `Nature`, `Wind`, `Drop`.
- Do not insert or update `cards` from this workflow. Assume the needed cards already exist.
- Prefer extending existing shared abstractions over copying large blocks of logic.
- If a new prefab, prefab component, or prefab parameter block is needed, hand that part to `make-prefab`.
