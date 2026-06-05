# Database Schema & Parameters Reference

This document describes the database schema, key tables, and parameter system used to configure gameplay values in the Word Online game server.

---

## Core Tables

The DB schema is defined in [schema.sql](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/resources/sql/schema.sql). The key gameplay-related tables are described below:

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
  - Columns: `id` (bigserial), `name` (varchar).
  - **Rule**: `magics.name` value must match the corresponding Spring magic bean component name in lowercase (e.g. `fire_shot`, `leafair`).
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
  - Columns: `id` (bigserial), `name` (varchar, unique, e.g., `leafair_prefab`).
- **`parameters`**: Defines parameter names.
  - Columns: `id` (bigserial), `name` (varchar, unique, e.g., `damage`, `radius`, `speed`, `range`, `mana_cost`).
- **`parameter_values`**: Junction table mapping objects and parameters to numeric values.
  - Columns: `id`, `parameter_id`, `game_object_id`, `value` (double).

### Querying Parameters at Runtime
At runtime, classes can access these values using the [Parameters](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/Parameters.java) domain object.
- **API Call**: `parameters.getValue(objectName, parameterName)`
- **Example Usage**:
  `int damage = (int) parameters.getValue("drop", "damage");`
  `float range = (float) parameters.getValue("fire_shot", "range");`
- **Scaffolding Requirements**: When adding a new magic spell, default values for parameters (e.g. `mana_cost`, `range`) must be populated in `parameter_values` using the spell's Spring bean name (for casting validations) and the spawned entity's prefab name (for radius/damage calculations).
