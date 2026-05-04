# Magic SQL

Use this when a new magic also needs database registration.

Store each magic in its own file under `src/main/resources/sql/magic/<magic_name>.sql`.

## Rules

- `magics.name` must equal the Spring magic bean name.
- `cards.name` must use the stored display names from the `cards` table, with the first letter capitalized.
- Only modify `magics` and `magic_cards`.
- Assume the required rows already exist in `cards`.
- Prefer idempotent SQL so the query can be re-run safely.

## Leafair Example

`leafair` is a `Nature + Drop` recipe, so the SQL is:

```sql
WITH inserted_magic AS (
    INSERT INTO magics(name)
    SELECT 'leafair'
    WHERE NOT EXISTS (
        SELECT 1
        FROM magics
        WHERE name = 'leafair'
    )
    RETURNING id
),
target_magic AS (
    SELECT id FROM inserted_magic
    UNION ALL
    SELECT id
    FROM magics
    WHERE name = 'leafair'
),
target_cards AS (
    SELECT id
    FROM cards
    WHERE name IN ('Nature', 'Drop')
)
INSERT INTO magic_cards(magic_id, card_id)
SELECT tm.id, tc.id
FROM target_magic tm
CROSS JOIN target_cards tc
WHERE NOT EXISTS (
    SELECT 1
    FROM magic_cards mc
    WHERE mc.magic_id = tm.id
      AND mc.card_id = tc.id
);
```

## Generic Template

Replace `magic_name` and the card list:

```sql
WITH inserted_magic AS (
    INSERT INTO magics(name)
    SELECT 'magic_name'
    WHERE NOT EXISTS (
        SELECT 1
        FROM magics
        WHERE name = 'magic_name'
    )
    RETURNING id
),
target_magic AS (
    SELECT id FROM inserted_magic
    UNION ALL
    SELECT id
    FROM magics
    WHERE name = 'magic_name'
),
target_cards AS (
    SELECT id
    FROM cards
    WHERE name IN ('CardA', 'CardB', 'CardC')
)
INSERT INTO magic_cards(magic_id, card_id)
SELECT tm.id, tc.id
FROM target_magic tm
CROSS JOIN target_cards tc
WHERE NOT EXISTS (
    SELECT 1
    FROM magic_cards mc
    WHERE mc.magic_id = tm.id
      AND mc.card_id = tc.id
);
```

## Parameter SQL Template

Use this when the new prefab needs its own DB-backed stats.

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
