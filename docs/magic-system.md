# Magic System Reference

This document explains the spell-casting, card recipe parsing, and magic execution processes in the Word Online game server.

---

## The Spell Casting Flow

Casting magic follows a strict validation pipeline from client input to runtime execution:

```
Client (WS JSON)
   |
   v
[InputController] -- (Validates authorization token)
   |
   v
[MagicInputHandler.handleInput()]
   |
   +--> 1. Check if user holds cards in hand (PlayerData.validCardsUse)
   +--> 2. Parse card combo (DatabaseMagicParser.parseMagic)
   |      |--> Check if spell recipe is valid
   |      |--> Check if user has unlocked the magic in their inventory
   +--> 3. Calculate target distance vs spell range parameters
   +--> 4. Deduct cards and mana cost (PlayerData.useCards)
   +--> 5. Call magic.run()
   +--> 6. Return cards to the bottom of the player's deck
   +--> 7. Publish execution status (STOMP event)
```

---

## Recipe Parsing & Database Binding

Card combinations are evaluated by [DatabaseMagicParser.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/magic/parser/DatabaseMagicParser.java):

1. **Card Sequence Sorting**: During comparison, card sequences are sorted alphabetically (e.g. `[Wind, Nature]` and `[Nature, Wind]` yield the same key).
2. **Building Each Magic**: At startup, `DatabaseMagicParser` queries the `magics` table and, per row, reads `cast_kind` together with the name and `prefab` of the game object the magic points at.
   - `cast_kind` is empty or `Code`: it looks for a Spring component bean matching `magics.name` (e.g. `@Component("spirit_bomb")` for `SpiritBombMagic`). No such bean means a warning and that magic is left out.
   - Anything else: no bean is looked up. `DatabaseMagicFactory` builds the family's magic - `SpawnMagic`, `SummonMagic`, `DropMagic`, `ShotMagic`, `ExplosionMagic` - from the prefab and the spawn height. A prefab name that is not a `PrefabType` constant, or a magic pointing at no game object, is a warning and that magic alone is skipped.
   - Either way the result is registered in the sorted recipe map: `magicHashMap.put(sortedCards, magic)`.
3. **Ownership Validation**: Before executing a parsed spell, `parseMagic` verifies the player owns the magic using `magicRepository.existUserMagic(userId, magic.id)`.

---

## Spell Execution (`Magic.run()`)

All spells extend [Magic.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/magic/Magic.java) and implement:
`public abstract void run(GameContext gameContext, Master master, Vector3 position);`

### Basic Magic Subclasses:
- **`AbstractDropMagic`**:
  - Spawns a GameObject at target position, at the spawn height it was built with (`GameConfig.DROP_MAGIC_INITIAL_HEIGHT` unless `magic_parameters.spawn_height` says otherwise).
  - The object falls under the control of a `Drop` component.
- **`AbstractShotMagic`**:
  - Spawns a projectile GameObject at the caster's location.
  - Attaches a `Shot` component and sets target coordinates to calculate flight vectors.
- **`AbstractSpawnMagic`**: spawns `quantity` units at the aim point, scattered when more than one, at the spawn height (0 by default).
- **`AbstractSummonMagic`**: puts one building at the aim point. With no `spawn_height` it keeps the aim point's own height; with one it uses that height, which is how `cannon`, `tower` and `dragon_tower` stay on the ground.

---

## Scaffolding Guidelines

A spell whose whole behavior is "which family, which prefab" needs no Java class. Adding one is a
row, not a deploy:

1. **Define a Prefab Type**:
   - If the spell puts a new physical entity on the field, define a new `PrefabType` and implement a corresponding `PrefabInitializer` (see [Prefab System Reference](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/prefab-system.md)).
2. **Database Registration**:
   - Add a versioned Flyway migration under `../database/migration/` and publish it before game-server code that requires the new magic.
   - Seed the `game_objects` row and set its `prefab` to the `PrefabType` constant name.
   - Seed `magics` with `cast_kind` and `game_object_id`, and link card IDs in `magic_cards`.
   - Register the default gameplay parameters (such as `mana_cost`, `range`, `damage`, `radius`) in `parameter_values`, and a `spawn_height` in `magic_parameters` if the family default is wrong for this spell.
3. **Only If A Class Is Needed**:
   - A spell that does something the five families do not - splitting its prefab by target, attaching a component to the caster, chaining several objects - is `cast_kind = 'Code'`.
   - Save the class under the correct element family package (e.g. `domain/magic/implement/drop/`) and annotate with `@Component("magic_name")` in lowercase, matching `magics.name` exactly.
