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

The game uses deterministic lockstep. Each client runs the same simulation. The
server validates and orders frame inputs, compares peer hashes, and relays
confirmed frames without simulating authoritative game state.

### Data Communication Flow

1. **Client Simulation**: Each client deterministically simulates the confirmed frame and hashes its resulting state.
2. **Frame Submission**: The client sends its next-frame inputs and previous-frame hash to `/app/game/lockstep/input/{sessionId}/{userId}`.
3. **Server Validation**: [LockstepInputController](../src/main/java/com/wordonline/server/game/controller/LockstepInputController.java) authenticates the participant. `InputRelayLoop` validates the protocol and frame window, then waits for the human quorum.
4. **Frame Confirmation**: The server orders inputs by user and sequence, compares peer hashes, and publishes the confirmed frame to:
   - `/topic/game/{sessionId}/frameInfos/{userId}` (for individual player views).
   - `/topic/game/{sessionId}/frameInfos` (for broadcast views/spectators).

### Protocol Specifications
- **WebSocket Connection**: Handled via [WebSocketConfig.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/websocket/WebSocketConfig.java). Securing channels is managed by [JwtChannelInterceptor.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/websocket/JwtChannelInterceptor.java) and [JwtHandshakeInterceptor.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/websocket/JwtHandshakeInterceptor.java).
- **Client Input Target**: `/game/lockstep/input/{sessionId}/{userId}`
- **Server Broadcast Target**: `/game/{sessionId}/frameInfos/{userId}` (for player-specific updates) and `/game/{sessionId}/frameInfos` (for spectator broadcasts).
