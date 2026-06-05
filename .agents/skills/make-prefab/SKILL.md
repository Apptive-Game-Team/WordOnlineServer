---
name: make-prefab
description: Scaffold a new game prefab in this repository when the user asks to create or add a prefab or when a new magic needs its own prefab. Use for requests like "make prefab", "add a prefab initializer", or "wire a new magic prefab" that need a PrefabType entry, a PrefabInitializer, and optional prefab-specific runtime components. Use make-parameter for DB-backed stats and parameter SQL.
---

# Make Prefab

Use this skill when adding a new prefab to `word-online/dev/game-server`.

## Scope

This skill covers the minimum scaffold:

- add the matching `PrefabType` entry in `src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabType.java`
- create the matching `PrefabInitializer` under `src/main/java/com/wordonline/server/game/domain/object/prefab/implement/...`
- add supporting component classes when the prefab behavior is not already provided by existing shared components
- prepare prefab tags through `tags` and `game_object_tags` SQL when adding a DB-backed prefab object

For prefab parameters and idempotent parameter SQL, use `make-parameter`.

Use `make-magic` alongside this skill when the prefab is being introduced as part of a new magic.

## Workflow

1. Inspect a nearby prefab of the same family first.
2. Keep Spring bean names consistent:
   `@Component("prefab_name_prefab")` for prefab initializers.
3. Add the new `PrefabType` enum constant with its bean name.
4. Implement the prefab initializer using existing helpers like `addComponent(...)` and `addCollider(...)`.
5. If the behavior is custom, create a dedicated component in `src/main/java/com/wordonline/server/game/domain/object/component/magic` or the nearest existing component package.
6. Add prefab tag SQL for every new `game_objects` row. Read `references/prefab-tags.md` and update that file when introducing a new reusable tag.
7. Verify compilation with `./gradlew compileJava` when possible.

## File Patterns

- Drop prefab:
  `object/prefab/implement/drop/<Name>PrefabInitializer.java`
- Shot prefab:
  `object/prefab/implement/<element-or-group>/<Name>PrefabInitializer.java`
- Custom runtime behavior:
  `object/component/magic/<Name>.java`

## Constraints

- Match existing naming conventions exactly: `LeafairPrefabInitializer`, `PrefabType.Leafair`.
- Prefer extending existing shared abstractions over copying large blocks of logic.
- When this prefab is part of a new magic, keep the magic registration itself in `make-magic`; this skill only owns the prefab side.
- Do not encode text tags as numeric `parameter_values`. Tags belong in `tags` and `game_object_tags`.
