# Prefab Tags

Use this when a new prefab creates or depends on a `game_objects` row.

Add tag changes to a new versioned migration under `../database/migration/`.
Do not add production SQL to game-server resources.

## Tables

- `tags`: reusable tag dictionary.
- `game_object_tags`: many-to-many mapping from `game_objects` to `tags`.

## Existing Tags

Type tags:
- `TYPE_Unit`: spawned gameplay object that can act, be targeted, collide for gameplay, or apply gameplay effects.
- `TYPE_Data`: parameter-only data row or helper row that should not be treated as a unit.
- `TYPE_Card`: card/magic recipe parameter row.

Category tags:
- `CAT_Small`: small unit.
- `CAT_Medium`: medium unit.
- `CAT_Large`: large unit.
- `CAT_Melee`: melee unit.
- `CAT_Ranged`: ranged unit or attack building.
- `CAT_Tank`: high-health/frontline unit.
- `CAT_Flying`: aerial unit.
- `CAT_CC`: crowd-control or displacement role.
- `CAT_AoE`: area damage, area buff, or repeated area effect.
- `CAT_Building`: stationary build/summon structure.

## Rules

- Add at least one `TYPE_*` tag for every new `game_objects` row.
- Add one or more `CAT_*` tags for `TYPE_Unit` rows when the role is known.
- Prefer existing tags. Add a new tag only when the game design needs a reusable category not listed above.
- If a new reusable tag is introduced, update this file in the same change.
- Use the gameplay parameter key as `game_objects.name`, usually the key used in `parameters.getValue(...)`.
- Use the next unused Flyway version and never edit an applied migration.

## SQL Template

```sql
WITH required_tags AS (
    SELECT tag_name
    FROM (
        VALUES
            ('TYPE_Unit'),
            ('CAT_Small'),
            ('CAT_Ranged')
    ) AS tags(tag_name)
),
inserted_tags AS (
    INSERT INTO tags(name)
    SELECT tag_name
    FROM required_tags rt
    WHERE NOT EXISTS (
        SELECT 1
        FROM tags t
        WHERE t.name = rt.tag_name
    )
    RETURNING id, name
),
target_tags AS (
    SELECT id, name FROM inserted_tags
    UNION ALL
    SELECT t.id, t.name
    FROM tags t
    JOIN required_tags rt ON rt.tag_name = t.name
),
target_game_object AS (
    SELECT id
    FROM game_objects
    WHERE name = 'game_object_name'
)
INSERT INTO game_object_tags(game_object_id, tag_id)
SELECT tgo.id, tt.id
FROM target_game_object tgo
JOIN target_tags tt ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM game_object_tags got
    WHERE got.game_object_id = tgo.id
      AND got.tag_id = tt.id
);
```
