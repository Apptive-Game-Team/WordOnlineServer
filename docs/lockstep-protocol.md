# Lockstep Relay Protocol v1

All game sessions use the lockstep relay. The server requires non-empty
`LOCKSTEP_SIMULATION_VERSION` and `LOCKSTEP_CONFIG_VERSION` values. There is no
authoritative simulation fallback.

## Session bootstrap

Before simulation starts, each human sends exact version readiness to
`/app/game/lockstep/ready/{sessionId}/{userId}`.

```json
{
  "protocolVersion": 1,
  "simulationVersion": "client-347",
  "configVersion": "game-data-sha256"
}
```

Any mismatch aborts the session with `version-mismatch`; there is no protocol
fallback. If the human quorum does not become ready before
`LOCKSTEP_READY_TIMEOUT_MS`, the server aborts with `ready-timeout`.

After the ready barrier, server sends `lockstepSessionStart` to both human
player destinations and the spectator destination. Negative bot IDs do not
receive network messages and do not participate in barriers.

```json
{
  "type": "lockstepSessionStart",
  "protocolVersion": 1,
  "simulationVersion": "client-347",
  "configVersion": "game-data-sha256",
  "rngSeed": 1234,
  "initialFrame": 1,
  "sessionType": "PVP",
  "leftUserId": 10,
  "rightUserId": 20,
  "leftCards": ["Fire"],
  "rightCards": ["Water"],
  "bootstrapEvents": [
    {"sequence": 0, "type": "SPAWN_PLAYER", "master": "LeftPlayer", "position": {"x": 1, "y": 0, "z": 5}, "scenarioId": null},
    {"sequence": 1, "type": "SPAWN_PLAYER", "master": "RightPlayer", "position": {"x": 17, "y": 0, "z": 5}, "scenarioId": null}
  ]
}
```

PVE replaces the right-player spawn with ordered `START_PVE_SCENARIO`. Clients
load that scenario from the versioned deterministic config.

## Frame submission

Destination: `/app/game/lockstep/input/{sessionId}/{userId}`

```json
{
  "protocolVersion": 1,
  "frameNum": 42,
  "previousFrameHash": "0E5CF22FA41895DB",
  "inputs": [
    {
      "sequence": 0,
      "type": "useMagic",
      "id": 123,
      "cards": ["Fire"],
      "position": {"x": 1.0, "y": 2.0, "z": 0.0}
    }
  ]
}
```

The authenticated principal must equal the path user ID. The server rejects
non-participants, wrong protocol versions, late frames, submissions beyond the
configured future window, duplicate input sequences, and conflicting retries.
An identical retry is idempotent. Input `type` is a closed protocol enum;
version 1 supports `useMagic`.

## Confirmed frame

After every required human submits, the server sorts inputs by `userId` and
then `sequence`. Spectators receive the result but never block confirmation.

```json
{
  "type": "confirmedFrame",
  "protocolVersion": 1,
  "frameNum": 42,
  "inputs": [],
  "previousFrameHashes": {"10": "hash", "20": "hash"},
  "hashMatched": true
}
```

The server compares peer hashes only. It has no authoritative simulation hash.
A single-human PVE or Practice session confirms frames without waiting for a
peer hash. Its client owns the deterministic bot simulation.

## Abort policy

The initial implementation does not invent missing inputs or choose an
authoritative peer. Input timeout, peer-hash mismatch, or relay interruption
broadcasts `lockstepAbort` and closes the loop. Competitive result/MMR is not
persisted because no loser is assigned. Abort reasons are `input-timeout`,
`ready-timeout`, `version-mismatch`, `peer-hash-mismatch`,
`participant-disconnected`, and `relay-interrupted`.

## Disconnect and observability

Frame resume is intentionally unsupported. A human disconnect after session
start aborts with `participant-disconnected`; reconnecting creates a new game
session. Structured logs include session start, confirmed frame, and abort.
`LockstepMetrics` counts started sessions, confirmed frames, and aborts by
reason for server health reporting.
