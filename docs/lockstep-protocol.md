# Lockstep Relay Protocol v1

All game sessions use the lockstep relay. The server requires non-empty
`LOCKSTEP_SIMULATION_VERSION` and `LOCKSTEP_CONFIG_VERSION` values. There is no
authoritative simulation fallback.

## Session bootstrap

Server sends `lockstepSessionStart` to both human player destinations and the
spectator destination. Negative bot IDs do not receive network messages and do
not participate in the frame barrier.

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
  "rightCards": ["Water"]
}
```

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
`peer-hash-mismatch`, and `relay-interrupted`.
