# Game Loop & Systems Reference

This document details the fixed-rate simulation loop, tick rate, and system update ordering inside the Word Online game server.

## Fixed Ticks & Delta Time

- **Tick Rate (FPS)**: The server runs at **20 FPS** (Frames Per Second).
- **Target Delta Time**: 50ms (0.05 seconds) per tick.
- **Delta Calculation**: Measured dynamically inside `GameLoop.run()` to account for execution lag, setting `deltaTime` in `GameContext` for physics integrations:
  `gameContext.setDeltaTime((System.currentTimeMillis() - startTime) / 1000.0f);`

---

## The Tick Update Sequence

[WordOnlineLoop.update()](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/service/WordOnlineLoop.java#L127-L159) defines the precise execution order of systems during each tick:

### 1. Early State & Card/Mana Operations
- **`frameDataSystem.earlyUpdate(gameContext)`**:
  - Calculates remaining game time.
  - Accumulates player mana: [ManaCharger.chargeMana(...)](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/service/ManaCharger.java#L38-L43) increases mana every `MANA_CHARGE_INTERVAL` (0.2s / 4 frames) by the player's mana charge rate, capped at max mana.
  - Draws cards: [CardDeck.drawCard(...)](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/service/CardDeck.java#L33-L44) draws cards every `cardDrawInterval` (default 1s / 20 frames) if the player's hand size is below `MAX_CARD_NUM` (6).

### 2. Gameplay Updates & Bot AI
- **`feverTimeSystem.update(gameContext)`**: If the remaining game time falls below the fever time threshold, it activates fever mode (accelerating card draw speeds).
- **`botSystem.update(gameContext)`**: Executes the AI agent ticks for training practice bots or disconnected human fallback bots.

### 3. Match Evaluation
- Evaluates if the match timer has expired.
- **`ResultChecker.checkResult()`**: Checks player health. If either player reaches 0 HP, it triggers game shutdown (`handleGameEnd()`).

### 4. Initialization of New Entities
- **`gameObjectStateInitialSystem.update(gameContext)`**: Scans for newly created GameObjects that are not yet marked `Idle` and switches them to the `Idle` status (allowing them to participate in updates and collisions).

### 5. Component Logic Updates
- **`componentUpdateSystem.update(gameContext)`**: Iterates over all active, non-destroyed GameObjects and invokes `update()` on all registered behaviors (e.g. cloud movements, projectile ticks, summon AI).

### 6. Physics Integration & Collision Processing
- **`physicSystem.update(gameContext)`**: Checks overlap states, resolves overlapping boundaries, triggers collision events, and integrates velocities into spatial positions.

### 7. Entity List Synchronization
- **`gameObjectAddRemoteSystem.update(gameContext)`**:
  - Removes destroyed GameObjects.
  - Mutates component lists by executing `flushComponents()` (calls `start()` or `onDestroy()` on newly added/removed components).
  - Appends newly instantiated GameObjects from the `gameObjectsToAdd` buffer to the active list.

### 8. State Sync
- **`buildSnapshot()`**: Gathers the updated state of all active GameObjects.
- **`frameDataSystem.lateUpdate(gameContext)`**: Transmits the update packet. If the frame number is a multiple of 10, [SyncFrameDataSystem](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/service/system/SyncFrameDataSystem.java) broadcasts a full snapshot to force client alignment.
