# Prefab Parameter SQL

Use this when a prefab needs database-backed stats or when a value should be tunable through `parameters`.

Place the SQL in a new versioned migration under `../database/migration/`.
Do not add production SQL to the game-server resources.

## Rules

- Reuse existing parameter names when possible.
- Add new parameter names only for values that should be tuned in data.
- Keep system defaults and shared engine constants in code.
- `game_objects.name` should match the prefab name or the lookup key used in `parameters.object(...)`.
- Prefer idempotent SQL so the query can be re-run safely.
- Use the next unused Flyway version and never edit an applied migration.

## Template

```sql
WITH inserted_game_object AS (
    INSERT INTO game_objects(name)
    SELECT 'game_object_name'
    WHERE NOT EXISTS (
        SELECT 1
        FROM game_objects
        WHERE name = 'game_object_name'
    )
    RETURNING id
),
target_game_object AS (
    SELECT id FROM inserted_game_object
    UNION ALL
    SELECT id
    FROM game_objects
    WHERE name = 'game_object_name'
),
required_parameters AS (
    SELECT parameter_name
    FROM (
        VALUES
            ('hp'),
            ('speed'),
            ('damage')
    ) AS params(parameter_name)
),
inserted_parameters AS (
    INSERT INTO parameters(name)
    SELECT parameter_name
    FROM required_parameters rp
    WHERE NOT EXISTS (
        SELECT 1
        FROM parameters p
        WHERE p.name = rp.parameter_name
    )
    RETURNING id, name
),
target_parameters AS (
    SELECT id, name FROM inserted_parameters
    UNION ALL
    SELECT p.id, p.name
    FROM parameters p
    JOIN required_parameters rp ON rp.parameter_name = p.name
),
parameter_seed_values AS (
    SELECT *
    FROM (
        VALUES
            ('hp', 10.0),
            ('speed', 1.0),
            ('damage', 4.0)
    ) AS seed(parameter_name, parameter_value)
)
INSERT INTO parameter_values(game_object_id, parameter_id, value)
SELECT tgo.id, tp.id, psv.parameter_value
FROM target_game_object tgo
JOIN target_parameters tp ON TRUE
JOIN parameter_seed_values psv ON psv.parameter_name = tp.name
WHERE NOT EXISTS (
    SELECT 1
    FROM parameter_values pv
    WHERE pv.game_object_id = tgo.id
      AND pv.parameter_id = tp.id
);
```
