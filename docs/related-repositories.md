# Related Repositories

This repository is `WordOnlineServer`, the actual game session server for Word Online.

Related repositories:
- `Apptive-Game-Team/WordOnlineMatching`: lobby and matchmaking server.
- `Apptive-Game-Team/WordOnlineClient`: Unity client consuming lobby and game-server APIs.
- `Apptive-Game-Team/AccountServer`: account/member service used for authentication and player identity.
- `Apptive-Game-Team/WordOnlineDatabase`: database migration source of truth.

## Coordination Notes

- Matchmaking flow starts in `WordOnlineMatching` and hands players off to this server for live game sessions.
- Client-facing gameplay behavior must stay aligned with `WordOnlineClient`, especially for protocol, DTO, and debug changes.
- Authentication or member-data changes may require matching updates in `AccountServer`.
- Production schema, seed, and gameplay data changes must be added to
  `WordOnlineDatabase/migration`; this repository keeps test-only SQL fixtures
  only.
