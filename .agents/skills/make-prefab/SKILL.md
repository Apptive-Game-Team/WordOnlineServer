---
name: make-prefab
description: Scaffold a new game prefab in this repository when the user asks to create or add a prefab or when a new magic needs its own prefab. Use for requests like "make prefab", "add a prefab initializer", or "wire a new magic prefab" that need a PrefabType entry, a PrefabInitializer, and optional prefab-specific runtime components or parameter SQL.
---

# Make Prefab

Use this skill when adding a new prefab to `word-online/dev/game-server`.

## Scope

This skill covers the minimum scaffold:

- add the matching `PrefabType` entry in `src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabType.java`
- create the matching `PrefabInitializer` under `src/main/java/com/wordonline/server/game/domain/object/prefab/implement/...`
- add supporting component classes when the prefab behavior is not already provided by existing shared components
- prepare prefab-specific `game_objects`, `parameters`, and `parameter_values` SQL when the prefab needs DB-backed stats

Use `make-magic` alongside this skill when the prefab is being introduced as part of a new magic.

## Workflow

1. Inspect a nearby prefab of the same family first.
2. Keep Spring bean names consistent:
   `@Component("prefab_name_prefab")` for prefab initializers.
3. Add the new `PrefabType` enum constant with its bean name.
4. Implement the prefab initializer using existing helpers like `addComponent(...)` and `addCollider(...)`.
5. If the behavior is custom, create a dedicated component in `src/main/java/com/wordonline/server/game/domain/object/component/magic` or the nearest existing component package.
6. If the prefab needs DB-backed stats, append idempotent parameter SQL to the owning magic file under `src/main/resources/sql/magic/<magic_name>.sql` when this prefab belongs to a magic. Keep pure implementation constants in code when DB tuning is not needed.
7. For SQL examples and reusable templates, read `references/prefab-parameter-sql.md`.
8. Verify compilation with `./gradlew compileJava` when possible.

## File Patterns

- Drop prefab:
  `object/prefab/implement/drop/<Name>PrefabInitializer.java`
- Shot prefab:
  `object/prefab/implement/<element-or-group>/<Name>PrefabInitializer.java`
- Custom runtime behavior:
  `object/component/magic/<Name>.java`

## Constraints

- Match existing naming conventions exactly: `LeafairPrefabInitializer`, `PrefabType.Leafair`.
- Reuse nearby parameter keys when possible. Do not introduce new DB-backed parameter names unless the task explicitly includes data changes.
- Prefer extending existing shared abstractions over copying large blocks of logic.
- For parameter SQL, the `game_objects.name` should usually match the prefab or lookup key used in `parameters.getValue(...)`.
- When this prefab is part of a new magic, keep the magic registration itself in `make-magic`; this skill only owns the prefab side.
