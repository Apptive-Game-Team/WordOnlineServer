---
name: make-magic
description: Scaffold a new game magic in this repository when the user asks to create or add a magic. Use for requests like "make magic", "add a new magic", or "scaffold a drop/build/shoot magic" that need a Magic class, a PrefabType entry, and a PrefabInitializer wired into the existing Spring bean naming pattern.
---

# Make Magic

Use this skill when adding a new magic to `word-online/dev/game-server`.

## Scope

This skill covers the minimum scaffold:

- create the `Magic` class under `src/main/java/com/wordonline/server/game/domain/magic/implement/...`
- add the matching `PrefabType` entry in `src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabType.java`
- create the matching `PrefabInitializer` under `src/main/java/com/wordonline/server/game/domain/object/prefab/implement/...`
- prepare the SQL needed to register the magic in `magics` and connect its recipe in `magic_cards`

Add supporting component classes when the magic behavior is not already provided by existing shared components.

## Workflow

1. Inspect a nearby magic of the same family first.
2. Keep Spring bean names consistent:
   `@Component("magic_name")` for magic classes and `@Component("prefab_name_prefab")` for prefab initializers.
3. Choose the correct base class before writing code:
   `AbstractDropMagic`, `AbstractShotMagic`, `AbstractExplosionMagic`, `AbstractSummonMagic`, or a direct `Magic` subclass when needed.
4. Add the new `PrefabType` enum constant with its bean name.
5. Implement the prefab initializer using existing helpers like `addComponent(...)` and `addCollider(...)`.
6. If the behavior is custom, create a dedicated component in `src/main/java/com/wordonline/server/game/domain/object/component/magic`.
7. Write the SQL registration query. Only modify `magics` and `magic_cards`. Use the magic bean name as `magics.name`, and use card names exactly as stored in `cards.name` with leading capitals such as `Nature`, `Wind`, and `Drop`.
8. For SQL examples and a reusable template, read `references/magic-sql.md`.
9. Verify compilation with `./gradlew compileJava` when possible.

## File Patterns

- Drop magic:
  `domain/magic/implement/drop/<Name>Magic.java`
  `object/prefab/implement/drop/<Name>PrefabInitializer.java`
- Shoot magic:
  `domain/magic/implement/shoot/<Name>Magic.java`
  `object/prefab/implement/<element-or-group>/<Name>PrefabInitializer.java`
- Custom runtime behavior:
  `object/component/magic/<Name>.java`

## Constraints

- Match existing naming conventions exactly: `LeafairMagic`, `LeafairPrefabInitializer`, `PrefabType.Leafair`.
- Reuse nearby parameter keys when possible. Do not introduce new DB-backed parameter names unless the task explicitly includes data changes.
- `magics.name` must match the Spring magic bean name exactly, for example `leafair`.
- `cards.name` uses leading-capital card names, for example `Nature`, `Wind`, `Drop`.
- Do not insert or update `cards` from this workflow. Assume the needed cards already exist.
- Prefer extending existing shared abstractions over copying large blocks of logic.
