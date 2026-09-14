# Database Schema & Parameters Reference

This document describes the database schema, key tables, and parameter system used to configure gameplay values in the Word Online game server.

---

## Core Tables

The shared game DB schema and data migrations are defined in the
[database repository](../../database/migration). The key gameplay-related
tables are described below:

```
                  +------------------+
                  |      users       |
                  +------------------+
                            |
                            | (owns)
                            v
+------------+    +------------------+    +------------+
|   cards    | <--|   user_cards     |    |   magics   |
+------------+    +------------------+    +------------+
  |        ^                                |        ^
  |        | (maps recipes)                 |        | (owns spells)
  |      +---------------+                  |      +---------------+
  +------|  magic_cards  |                  +------|  user_magics  |
         +---------------+                         +---------------+
```

### 1. Card & Magic Inventories
- **`cards`**: Contains element cards used for recipes.
  - Columns: `id` (bigint), `name` (varchar), `card_type` (enum 'Magic', 'Type').
- **`magics`**: Holds spells registered in the game.
  - Columns: `id` (bigserial), `name` (varchar), `cast_kind` (varchar, nullable), `game_object_id` (bigint, nullable, FK -> `game_objects`).
  - `cast_kind` is how the server builds the spell: `Shot`, `Drop`, `Explosion`, `Summon`, `Spawn`, or `Code`. Anything but `Code` is built from data and needs no Java class; `Code` (and an empty `cast_kind`) is looked up as a Spring bean.
  - `game_object_id` points at what the spell creates. The prefab name comes from that row's `game_objects.prefab`, and a spawn family reads its `quantity` from that row's parameters.
  - **Rule**: a `Code` magic's `magics.name` must match the corresponding Spring magic bean component name in lowercase (e.g. `spirit_bomb`, `vine_toss`).
- **`magic_cards`**: Junction table mapping card combinations to magic spells.
  - Columns: `id`, `magic_id`, `card_id`.
- **`user_magics`**: Junction table mapping which magic spells are unlocked/owned by each user.
  - Columns: `id`, `user_id`, `magic_id`.

### 2. Player Progress & Decks
- **`users`**: User account credentials, statistics, status, and selected deck reference.
  - Columns: `id`, `mmr` (default 1000), `status` (varchar: 'Online', 'OnMatching', 'OnPlaying').
- **`decks`**: Saved card decks.
  - Columns: `id`, `name`, `user_id`.
- **`deck_cards`**: Junction table detailing which cards are in a user's deck.
  - Columns: `id`, `deck_id`, `card_id`, `count`.

---

## The Dynamic Parameter System

To tweak gameplay metrics without re-compiling Java code, dimensions like range, damage, speed, and mana costs are stored in the DB and loaded dynamically at startup:

- **`game_objects`**: Defines physical objects/prefabs in the game.
  - Columns: `id` (bigserial), `name` (varchar, unique, e.g., `leafair_prefab`), `prefab` (varchar, nullable).
  - `prefab` holds a `PrefabType` enum constant name (e.g. `EmberSpirit`, `GroundCannon`). It is not the prefab bean name and not always the object's own name: `PrefabType.EmberSpirit` has bean name `fire_slime_prefab`, and the magic that summons it reads its `quantity` from the `ember_spirit` row.
- **`parameters`**: Defines parameter names.
  - Columns: `id` (bigserial), `name` (varchar, unique, e.g., `damage`, `radius`, `speed`, `range`, `mana_cost`).
- **`parameter_values`**: Junction table mapping objects and parameters to numeric values.
  - Columns: `id`, `parameter_id`, `game_object_id`, `value` (double).
- **`magic_parameters`**: The same thing for a magic rather than an object - values the cast itself needs.
  - Columns: `id`, `magic_id`, `parameter_id`, `value` (double).
  - So far only `spawn_height`, which overrides the family default: `Spawn` starts at 0, `Drop` at `GameConfig.DROP_MAGIC_INITIAL_HEIGHT`, and `Summon` keeps the aim point's own height when no row exists.

### Querying Parameters at Runtime
At runtime, classes can access these values using the [Parameters](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/Parameters.java) domain object.
- **API Call**: `parameters.getValue(objectName, parameterName)`
- **Example Usage**:
  `int damage = (int) parameters.getValue("drop", "damage");`
  `float range = (float) parameters.getValue("fire_shot", "range");`
- **Scaffolding Requirements**: When adding a new magic spell, default values for parameters (e.g. `mana_cost`, `range`) must be populated in `parameter_values` using the spell's Spring bean name (for casting validations) and the spawned entity's prefab name (for radius/damage calculations).
