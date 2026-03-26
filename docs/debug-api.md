# Debug API Specification

This document describes the debug-only HTTP APIs for summoning magic, spawning prefabs, and listing available identifiers.

## Authorization

All endpoints require `WORDONLINE_ADMIN` or `SUPER_ADMIN` authority (enforced by `DebugController`).

## Endpoints

### POST `/api/debug/magic`

Summon a magic into a running session.

**Request body**
```json
{
  "sessionId": "debug-1",
  "master": "LeftPlayer",
  "magicId": 12,
  "position": { "x": 1.0, "y": 2.0, "z": 0.0 }
}
```

**Required fields**
- `sessionId` (string)
- `master` (enum: `LeftPlayer`, `RightPlayer`, `None`)
- `magicId` (number, must be positive)
- `position` (Vector3)

**Response**
```json
{ "success": true, "message": "Magic summoned successfully." }
```

**Error responses**
- `400` with `{ "success": false, "message": "Session not found or not running." }`
- `400` with `{ "success": false, "message": "Magic id is required." }`
- `400` with `{ "success": false, "message": "Magic id must be positive." }`
- `400` with `{ "success": false, "message": "Magic not found for id: <id>" }`

### POST `/api/debug/prefab`

Spawn a prefab into a running session.

**Request body**
```json
{
  "sessionId": "debug-1",
  "master": "RightPlayer",
  "prefabId": "rock_golem_prefab",
  "position": { "x": 3.0, "y": 4.0, "z": 0.0 }
}
```

**Required fields**
- `sessionId` (string)
- `master` (enum: `LeftPlayer`, `RightPlayer`, `None`)
- `prefabId` (string; use the beanName from `/api/debug/prefabs`)
- `position` (Vector3)

**Response**
```json
{ "success": true, "message": "Prefab spawned successfully." }
```

**Error responses**
- `400` with `{ "success": false, "message": "Session not found or not running." }`
- `400` with `{ "success": false, "message": "Prefab id is required." }`
- `400` with `{ "success": false, "message": "Prefab not found for id: <id>" }`

### GET `/api/debug/magics`

List available magics for use in `/api/debug/magic`.

**Response**
```json
[
  { "id": 1, "name": "fire_shot" },
  { "id": 2, "name": "ice_spike" }
]
```

### GET `/api/debug/prefabs`

List available prefabs for use in `/api/debug/prefab`.

**Response**
```json
[
  { "id": "rock_golem_prefab", "name": "RockGolem" },
  { "id": "healing_totem_prefab", "name": "HealingTotem" }
]
```
