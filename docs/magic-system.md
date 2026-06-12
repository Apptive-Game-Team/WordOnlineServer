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
2. **Spring Bean Matching**: At startup, `DatabaseMagicParser` queries the `magics` table:
   - For each magic row, it checks if a Spring component bean matching `magics.name` exists (e.g., `@Component("leafair")` for [LeafairMagic.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/magic/implement/drop/LeafairMagic.java)).
   - Registers the bean in the sorted recipe map: `magicHashMap.put(sortedCards, magicBean)`.
3. **Ownership Validation**: Before executing a parsed spell, `parseMagic` verifies the player owns the magic using `magicRepository.existUserMagic(userId, magic.id)`.

---

## Spell Execution (`Magic.run()`)

All spells extend [Magic.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/magic/Magic.java) and implement:
`public abstract void run(GameContext gameContext, Master master, Vector3 position);`

### Basic Magic Subclasses:
- **`AbstractDropMagic`**:
  - Spawns a GameObject at target position, but sets initial height `Z` to `GameConfig.DROP_MAGIC_INITIAL_HEIGHT`.
  - The object falls under the control of a `Drop` component.
- **`AbstractShotMagic`**:
  - Spawns a projectile GameObject at the caster's location.
  - Attaches a `Shot` component and sets target coordinates to calculate flight vectors.

---

## Scaffolding Guidelines

When adding a new magic spell, developers use the following steps:

1. **Create the Magic Class**:
   - Save the class under the correct element family package (e.g. `domain/magic/implement/drop/`).
   - Annotate with `@Component("magic_name")` in lowercase.
2. **Database Registration**:
   - Add a versioned Flyway migration under `../database/migration/` and
     publish it before game-server code that requires the new magic.
   - Seed data into `magics` (naming must match the Spring component name exactly) and link card IDs in `magic_cards`.
   - Register the default gameplay parameters (such as `mana_cost`, `range`, `damage`, `radius`) in `parameter_values`.
3. **Define a Prefab Type**:
   - If the spell spawns a new physical entity, define a new `PrefabType` and implement a corresponding `PrefabInitializer` (see [Prefab System Reference](file:///Users/jeong-yunseong/development/word-online/dev/game-server/docs/prefab-system.md)).
