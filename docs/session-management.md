# Session Management Reference

This document describes how game sessions, threading, and player lifetimes are managed in the Word Online server.

## Threading Model

Every active game session runs on its own dedicated OS thread.
- Sessions do **not** run on the Spring MVC container threads.
- When a game session is started, a new `Thread` instance wrapped around a `GameLoop` runnable is started.
- This ensures CPU isolation between matches, but means CPU usage scales with the number of active sessions.

## Session Lifecycle

### 1. Creation
When two players are matched (or a user starts a practice session), [SessionService.createSession(...)](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/session/service/SessionService.java#L57-L73) is called:
1. Instantiates a [SessionObject](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/SessionObject.java) using the `SessionObjectFactory`.
2. Creates the appropriate `GameLoop` implementation (`WordOnlineLoop` for PvP/Practice, or `PveLoop` for PvE) using the `GameLoopFactory`.
3. Initializes the loop and attaches an `onLoopTerminated` callback.
4. Starts a new `Thread` with the loop.
5. Registers the session inside the `sessions` map (`ConcurrentHashMap`).

### 2. Monitoring
`SessionService` tracks the count of active sessions.
- Clients can query active sessions using `/api/debug/prefabs` and similar debug endpoints or through room information DTOs.
- Player connectivity states (Online, OnMatching, OnPlaying) are synchronized in the `users` table status column.

### 3. Termination & Cleanup
When the loop finishes executing (e.g., game end evaluated by `ResultChecker` or session closed):
1. `onLoopTerminated` is triggered.
2. The session is removed from the `sessions` map.
3. [StatisticService](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/statistic/service/StatisticService.java) records match statistics (game duration, winner/loser, elements cast) in the DB.
4. For PvE scenarios, the scenario is marked as completed in `userScenarioService` if the human player wins.
5. MMR ratings are updated (for PvP sessions), and user status is reset back to `Online`.
