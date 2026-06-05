# System Overview Reference

This document provides a high-level overview of the Word Online game server architecture, technology stack, and networking model.

## Technology Stack

- **Language**: Java 21 (Gradle-based build)
- **Framework**: Spring Boot 3.x
- **Data Access**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL (Production), H2 (Local / Test)
- **Messaging**: STOMP over WebSockets (Spring WebSocket Message Broker)
- **Testing**: JUnit Platform, Spring Security Test, Awaitility, H2

## Network Topology

The game operates on an authoritative server model. The client sends player inputs (cast requests, pings) to the server, and the server simulates the game logic, physics, and state updates, then sends frame/snapshot updates back to the clients.

### Data Communication Flow

1. **Client Action (Input)**: The client sends cast inputs or ping requests as STOMP JSON messages over a WebSocket connection to the server endpoint `/app/game/input/{sessionId}/{userId}`.
2. **Server Routing (Controller)**: [InputController](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/controller/InputController.java) receives the payload, validates user credentials, and forwards the command to the session's `MagicInputHandler`.
3. **Simulation Tick (Game Loop)**: The thread running `GameLoop` updates the spatial positions, resolves physics collisions, and mutates GameObject elements.
4. **State Broadcasting (Frame Update)**: At the end of every tick, the game server compiles frame events and incremental updates (mana, card drawing, and position changes). It publishes these payloads to:
   - `/topic/game/{sessionId}/frameInfos/{userId}` (for individual player views).
   - `/topic/game/{sessionId}/frameInfos` (for broadcast views/spectators).

### Protocol Specifications
- **WebSocket Connection**: Handled via [WebSocketConfig.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/websocket/WebSocketConfig.java). Securing channels is managed by [JwtChannelInterceptor.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/websocket/JwtChannelInterceptor.java) and [JwtHandshakeInterceptor.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/websocket/JwtHandshakeInterceptor.java).
- **Client Input Target**: `/game/input/{sessionId}/{userId}`
- **Server Broadcast Target**: `/game/{sessionId}/frameInfos/{userId}` (for player-specific updates) and `/game/{sessionId}/frameInfos` (for spectator broadcasts).
