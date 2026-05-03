# Magic SQL

Use this when a new magic also needs database registration.

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
