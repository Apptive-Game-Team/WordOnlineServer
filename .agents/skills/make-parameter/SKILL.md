---
name: make-parameter
description: Use when a prefab needs database-backed stats, parameter key naming, or idempotent SQL for prefab parameters. Use this to decide which values should become parameters versus code constants, and to keep prefab parameter SQL separate from prefab scaffolding and tags.
---

# Make Parameter

Use this skill when adding or changing DB-backed prefab stats in `word-online/dev/game-server`.

## Scope

- decide whether a value belongs in `parameters` or should stay a code constant
- reuse existing parameter names when possible
- add new `game_objects`, `parameters`, and `parameter_values` rows only when the prefab needs balance-tunable data
- keep prefab tags in `make-prefab`

## Workflow

1. Inspect nearby prefab implementations and existing parameter keys first.
2. Prefer existing `ParameterKey` / `GameObjectKey` names before adding new ones.
3. Keep fixed engine constants and shared system defaults in code.
4. Add idempotent SQL for the prefab's parameter rows.
5. Keep `game_objects.name` aligned with the prefab lookup key used by `parameters.object(...)`.
6. If the prefab is part of a new magic, keep magic registration in `make-magic`.
7. Validate compilation when the parameter change touches runtime code.

## References

- `references/prefab-parameter-sql.md`
- `src/main/java/com/wordonline/server/game/domain/parameter/ParameterKey.java`
- `src/main/java/com/wordonline/server/game/domain/parameter/GameObjectKey.java`

