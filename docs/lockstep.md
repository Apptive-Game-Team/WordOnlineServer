# Lockstep Networking

This document describes the lockstep networking model planned for Arcane Casters and what needs to change in both the server (`game/`) and client (`client/`) to implement it.

---

## Why Lockstep

The current model is **server-authoritative push**:

- The server runs the entire simulation and broadcasts state every frame.
- Clients are passive renderers — they display whatever the server sends.
- Inputs carry no frame number; the server processes them as they arrive.

Problems with this model:

| Problem | Cause |
|---|---|
| Visible input latency | Player submits card → server executes → state broadcast → client renders. Two round trips. |
| Jitter | State arrives at irregular intervals; client uses DOTween to hide it. |
| High bandwidth | Full delta objects + snapshot every 10 frames, every session. |
| No client prediction possible | Client has no simulation; it can't predict the result of its own input. |

**Lockstep** fixes this by moving the authoritative simulation to **both clients simultaneously**. The server becomes an **input relay**: it collects inputs from all players each frame, confirms them, and broadcasts them. Each client runs the identical deterministic simulation locally and produces identical state — no state sync required.

---

## Target Model: Client-Side Deterministic Lockstep

```
Before (server-authoritative push):
  Client A ──input──► Server ──simulate──► broadcast state ──► Client A
                                                              ──► Client B

After (client-side deterministic lockstep):
  Client A ──input(N)──► Server ──broadcast inputs(N)──► Client A  ──simulate──► state N
                                                       ──► Client B ──simulate──► state N
                                                                    (identical)
```

The server no longer simulates. It:
1. Collects inputs from both clients for frame N.
2. Once all inputs arrive (or timeout), broadcasts the confirmed input set for frame N.
3. Both clients execute the same simulation with those inputs → identical state.

The server retains one responsibility: **result arbitration**. It still receives a game-end signal and persists statistics.

---

## Prerequisite: Making the Simulation Deterministic

The current Java simulation is **not** deterministic across platforms or runs because:

| Issue | Location | Fix |
|---|---|---|
| `Math.random()` / `java.util.Random` | Magic effects, spawn spread | Replace with seeded deterministic RNG (LCG or xoshiro). Seed agreed on at session start. |
| `System.currentTimeMillis()` delta time | `GameLoop` delta time calculation | Replace with fixed delta: `dt = 1.0f / FPS` (50ms, constant) |
| Floating point order-dependence | Physics, position math | Use integer fixed-point positions (e.g. ×1000 milliunits) everywhere |
| HashMap iteration order | `ComponentUpdateSystem` iterates `gameContext.getGameObjects()` | Change to `List` (ordered, insertion-order) |
| `System.nanoTime()` for timing | Any timing in components | Replace with `frameNum`-based timing |

Once deterministic, the Java simulation is the **specification**. The client C# simulation must produce identical results given the same inputs and seed.

---

## Simulation Port: Java → C#

The simulation lives in `game/src/main/java/com/wordonline/server/game/domain/` and `game/service/system/`. The corresponding C# port goes in `client/Assets/Scripts/Simulation/`.

### What to port

| Java (server) | C# (client) | Notes |
|---|---|---|
| `GameObject` | `SimGameObject` | Pure C# class, not MonoBehaviour |
| `Component` and subclasses | `SimComponent` and subclasses | Same lifecycle: `Start`, `Update`, `OnDestroy` |
| `PrefabInitializer` | `SimPrefabInitializer` | Factory to build SimGameObjects |
| `SimplePhysics` | `SimPhysics` | `OverlapSphere`, `Raycast` — use same integer math |
| Magic system (`magic/`) | `SimMagic` namespace | All spell types, elemental chart |
| `ComponentUpdateSystem` | `SimComponentUpdateSystem` | Iterate list in insertion order |
| `PhysicSystem` | `SimPhysicSystem` | Collision, position update |
| `GameObjectAddRemoveSystem` | `SimGameObjectAddRemoveSystem` | |
| `ResultChecker` | `SimResultChecker` | Triggers local game-end |
| `FeverTimeSystem` | `SimFeverTimeSystem` | |

### What NOT to port (server-only concerns)

- WebSocket / STOMP infrastructure
- Session management, matchmaking
- Authentication, JWT
- Statistics persistence
- `FrameDataSystem`, `SyncFrameDataSystem` — no longer needed
- `BotSystem` — bot runs on server; its inputs arrive like any player's inputs

---

## New Message Protocol

### Session Start (Server → Client, once)

```json
{
  "type": "sessionStart",
  "frameNum": 0,
  "rngSeed": 1234567890,
  "leftUserId": 101,
  "rightUserId": 202,
  "leftCards": ["fire", "water", ...],
  "rightCards": ["ice", "rock", ...]
}
```

Both clients initialize their simulation with the same seed, same starting cards.

### Input (Client → Server, every frame)

```json
{
  "type": "input",
  "frameNum": 42,
  "userId": 101,
  "cards": ["fire", "lightning"],
  "position": { "x": 1.5, "y": 0.0, "z": 3.2 }
}
```

If a player takes no action this frame, they still send:
```json
{ "type": "input", "frameNum": 42, "userId": 101, "cards": [], "position": null }
```

### Confirmed Input Frame (Server → Both Clients)

```json
{
  "type": "confirmedFrame",
  "frameNum": 42,
  "inputs": {
    "101": { "cards": ["fire", "lightning"], "position": { "x": 1.5, "y": 0.0, "z": 3.2 } },
    "202": { "cards": [], "position": null }
  }
}
```

On receipt, both clients call `Simulation.Step(frameNum, inputs)` → identical state.

### Game End (Server → Both Clients)

```json
{
  "type": "result",
  "leftPlayer": "Win",
  "rightPlayer": "Lose",
  "mmrDto": { ... }
}
```

No more `FrameInfoDto`, `SyncInfoDto`, `UpdatedObjectDto`, `CreatedObjectDto`, `ProjectileDto` — these are eliminated entirely. The client renders its own simulation state.

---

## Server Changes

The server becomes much simpler.

### Remove
- `FrameDataSystem`, `SyncFrameDataSystem`
- `ComponentUpdateSystem`, `PhysicSystem`, `GameObjectAddRemoveSystem`, `FeverTimeSystem`, `MagicInputHandler`
- `SnapshotResponseDto`, `FrameInfoDto`, `SyncInfoDto`, all object DTOs

### Keep
- `InputBufferSystem` (new, from Phase 2) — collects per-frame inputs
- `ResultChecker` — detects disconnect/forfeit/timeout, sends result
- `SessionObject` — manages WebSocket connections
- `StatisticService` — persists game results
- `BotSystem` — bot generates inputs on behalf of bot players

### New: `InputRelayLoop`

Replaces `WordOnlineLoop`. Extremely simple:

```java
while (running) {
    long frameStart = System.currentTimeMillis();

    waitForInputs(frameNum, MAX_WAIT_MS);  // 100ms timeout
    Map<Long, InputRequestDto> inputs = inputBufferSystem.consume(frameNum);

    ConfirmedFrameDto confirmed = new ConfirmedFrameDto(frameNum, inputs);
    sessionObject.broadcastFrameInfo(confirmed);

    frameNum++;
    sleep(max(0, frameDuration - (currentTimeMillis() - frameStart)));
}
```

No simulation. Just collect → broadcast → next frame.

---

## Client Changes

### Simulation Layer (new)
`client/Assets/Scripts/Simulation/`

Pure C# classes, no Unity dependencies (no `MonoBehaviour`, no `Transform`). Mirror the server's domain objects.

Key entry point:
```csharp
public class SimWorld
{
    public void Init(SessionStartDto session) { ... }
    public void Step(int frameNum, Dictionary<long, InputDto> inputs) { ... }
    public SimGameObject[] GetObjects() { ... }
    public int GetHp(Master side) { ... }
    public List<CardType> GetCards(long userId) { ... }
}
```

### FrameClock (new)
`client/Assets/Scripts/GameScene/FrameClock.cs`

Same as Phase 1 plan — local frame counter, `SyncTo(serverFrame)` for drift correction.

### Input Flow (changed)

```
Player action
  → CardInputSender.Send(cards, pos, FrameClock.LocalFrame)
  → STOMP /app/game/input/{sessionId}/{userId}
  → (server collects)
  → ConfirmedFrameDto received by GeneralHandler
  → SimWorld.Step(frameNum, inputs)
  → Renderer reads SimWorld state → updates Unity scene
```

### GeneralHandler (changed)

Old message types (`frame`, `sync`, `magicValid`) are removed. New:

```csharp
case "sessionStart":
    simWorld.Init(JsonUtility.FromJson<SessionStartDto>(json));
    break;
case "confirmedFrame":
    ConfirmedFrameDto cf = JsonUtility.FromJson<ConfirmedFrameDto>(json);
    FrameClock.SyncTo(cf.frameNum);
    simWorld.Step(cf.frameNum, cf.inputs);
    renderer.Render(simWorld);
    break;
case "result":
    resultHandler.Handler(...);
    break;
```

### Renderer (changed)
Current `ObjectSpawner`, `ObjectUpdater`, `ObjectSyncer` read DTOs from the server. Instead they read from `SimWorld`:

```csharp
public class SimRenderer : MonoBehaviour
{
    void LateUpdate()
    {
        foreach (var obj in simWorld.GetObjects())
            RenderObject(obj);  // spawn/update/destroy Unity GameObjects to match sim state
    }
}
```

---

## Client Versioning: FrameClock

```csharp
public static class FrameClock
{
    public static int LocalFrame { get; private set; } = 0;
    private static int _pausedFrames = 0;

    public static void Tick()
    {
        if (_pausedFrames > 0) { _pausedFrames--; return; }
        LocalFrame++;
    }

    public static void SyncTo(int serverFrame)
    {
        int drift = LocalFrame - serverFrame;
        if (drift > 2)      _pausedFrames += drift;   // client ahead
        else if (drift < -2) LocalFrame = serverFrame; // client behind — snap
    }
}
```

`Tick()` is called each `FixedUpdate` (every 50ms). `SyncTo()` is called on every `confirmedFrame` message.

---

## Drift Correction Policy

| Condition | Action |
|---|---|
| `\|localFrame - serverFrame\| ≤ 2` | No correction — within tolerance |
| `localFrame > serverFrame + 2` | Pause `FrameClock` for the excess ticks |
| `localFrame < serverFrame - 2` | Snap `LocalFrame = serverFrame` immediately |

The tolerance matches the server's `MAX_WAIT_MS` (100ms = 2 frames). A client within one timeout window is considered in sync.

---

## Sequence Diagram: Single Frame Lifecycle

```
Client A          Server                Client B
   |                 |                     |
  Tick()             |                    Tick()
   |                 |                     |
   |--input(N)------>|                     |
   |                 |<------input(N)------|
   |                 |                     |
   |         waitForInputs(N, 100ms)       |
   |         (all inputs received)         |
   |                 |                     |
   |<--confirmed(N)--|                     |
   |                 |----confirmed(N)---->|
   |                 |                     |
  SyncTo(N)          |                  SyncTo(N)
  SimWorld.Step(N)   |              SimWorld.Step(N)
   |                 |                     |
  Render             |                   Render
   |                 |                     |
  Tick() → N+1       |                Tick() → N+1
```

---

## Migration Phases

### Phase 1 — Versioning + wire up frame numbers (no behavior change)

- Add `frameNum` to `InputRequestDto` (server + client)
- Server includes `frameNum` in all existing broadcasts
- Create `FrameClock.cs`; client reads `frameNum` from broadcasts and calls `SyncTo`
- **Verify**: log `[localFrame, serverFrame]` pairs — confirm they stay within ±1

### Phase 2 — Server becomes input relay

- Implement `InputBufferSystem`
- Implement `InputRelayLoop` (replaces `WordOnlineLoop`)
- Server sends `ConfirmedFrameDto` instead of state DTOs
- **Verify**: server logs show frame-by-frame input collection; no simulation code runs

### Phase 3 — Client simulation

- Port game domain to `client/Assets/Scripts/Simulation/`
- Implement `SimWorld`, all `SimComponent` types, `SimPhysics`, magic system
- Make determinism fixes (fixed dt, seeded RNG, ordered collections, fixed-point math)
- Client renders from `SimWorld` instead of server DTOs
- Remove `ObjectSpawner`/`ObjectUpdater`/`ObjectSyncer` server-dto paths; replace with `SimRenderer`
- **Verify**: run two clients; print `SimWorld.GetSnapshot()` hash each frame — hashes must be identical

### Phase 4 — Cleanup

- Remove all server-side simulation code (`ComponentUpdateSystem`, `PhysicSystem`, all game domain objects, frame DTOs)
- Remove client-side delta/sync handlers
- Remove `PingSender` (keepalive replaced by regular input messages)

---

## Files Modified Summary

### Server: Remove
`FrameDataSystem`, `SyncFrameDataSystem`, `ComponentUpdateSystem`, `PhysicSystem`, `GameObjectAddRemoveSystem`, `FeverTimeSystem`, `MagicInputHandler`, all game domain objects, `SnapshotResponseDto`, `FrameInfoDto`, `SyncInfoDto`, object DTOs.

### Server: Add/Change
| File | Change |
|---|---|
| `game/src/.../dto/input/InputRequestDto.java` | + `int frameNum` |
| `game/src/.../dto/confirmedframe/ConfirmedFrameDto.java` | **NEW** |
| `game/src/.../dto/session/SessionStartDto.java` | **NEW** (includes `rngSeed`) |
| `game/src/.../controller/InputController.java` | Buffer only, no execution |
| `game/src/.../service/system/InputBufferSystem.java` | **NEW** |
| `game/src/.../service/InputRelayLoop.java` | **NEW** (replaces WordOnlineLoop) |
| `game/src/.../domain/SessionObject.java` | Send `ConfirmedFrameDto` |
| `game/src/.../session/service/SessionService.java` | Send `sessionStart` on creation, include `rngSeed` |

### Client: Remove
`DeltaFrameHandler`, `SyncFrameHandler`, `ObjectSpawner` (server-dto path), `ObjectUpdater`, `ObjectSyncer`, `PingSender`.

### Client: Add/Change
| File | Change |
|---|---|
| `client/.../GameScene/FrameClock.cs` | **NEW** |
| `client/.../Simulation/SimWorld.cs` | **NEW** |
| `client/.../Simulation/SimGameObject.cs` | **NEW** |
| `client/.../Simulation/SimComponent.cs` | **NEW** (+ all component ports) |
| `client/.../Simulation/SimPhysics.cs` | **NEW** |
| `client/.../Simulation/magic/` | **NEW** (port of all magic types) |
| `client/.../GameScene/SimRenderer.cs` | **NEW** |
| `client/.../GameScene/Handler/GeneralHandler.cs` | Handle `confirmedFrame`, `sessionStart` |
| `client/.../GameScene/Card/CardInputSender.cs` | Tag input with `FrameClock.LocalFrame` |
