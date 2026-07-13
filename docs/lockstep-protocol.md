# Lockstep Relay Protocol v1

Lockstep is disabled by default. Enable it only with non-empty
`LOCKSTEP_SIMULATION_VERSION` and `LOCKSTEP_CONFIG_VERSION` values. Existing
sessions continue to use the authoritative state protocol while disabled.
This rollout enables only PVP sessions. PVE and Practice remain authoritative
until their deterministic bootstrap contract is implemented.

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
An identical retry is idempotent.

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
The frame buffer supports a single-human quorum without waiting for a peer
hash. PVE and Practice routing will use that behavior in a later rollout.

## Abort policy

The initial implementation does not invent missing inputs or choose an
authoritative peer. Input timeout, peer-hash mismatch, or relay interruption
broadcasts `lockstepAbort` and closes the loop. Competitive result/MMR is not
persisted because no loser is assigned.
