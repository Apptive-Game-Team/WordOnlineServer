# 2026-06-29 — Admin local session list endpoint

- Date: 2026-06-29
- GitHub Issue: #278
- Status: Draft

## Goal

Expose active game sessions from a local game server through an admin-authorized endpoint so the Unity admin page can fetch `http://localhost:7777` directly.

## Non-goals

- Do not change existing server-to-server `/api/server/game-sessions` behavior.
- Do not change session creation, lifecycle, or matching flow.

## Context / Constraints

- Existing session list data lives in `SessionService.getAllActiveSessionsInfo`.
- Existing server-to-server endpoint is `GET /api/server/game-sessions` and requires `WORDONLINE_SERVER`.
- Unity admin users have admin/debug authority, not server authority.
- `DebugController` already exposes admin-only debug APIs under `/api/debug`.

## Approach (Checklist)
- [x] **Step 0: Recon** (Inspect existing code, locate files)
- [x] **Step 1: Implementation** (Code changes, file paths)
- [x] **Step 2: Tests** (Unit tests, manual verification steps)
- [x] **Step 3: Rollout / Rollback** (Feature flags, migration steps)

## Validation
- **Commands run:** `./gradlew test`; `git diff --check`.
- **Expected output:** Build/test passes and `GET /api/debug/game-sessions` returns `RoomListDto`.
- **Actual output:** Gradle test build succeeded; diff whitespace check passed.

## Risks & Rollback
- **Risks:** Client and game PRs must land together for local direct fetch. Related client issue: Apptive-Game-Team/WordOnlineClient#331.
- **Rollback steps:** Revert the debug endpoint and client local fetch path.

## Open Questions
- None.
