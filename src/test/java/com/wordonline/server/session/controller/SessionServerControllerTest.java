package com.wordonline.server.session.controller;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.service.ServerStatusService;
import com.wordonline.server.server.service.ServerUrlProvider;
import com.wordonline.server.session.dto.CreateSessionRequest;
import com.wordonline.server.session.dto.SessionReadyResponse;
import com.wordonline.server.session.service.SessionCreationResult;
import com.wordonline.server.session.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionServerControllerTest {

    @Test
    void returnsReadySessionWithExternalConnectionInfo() {
        SessionService sessionService = mock(SessionService.class);
        ServerStatusService serverStatusService = mock(ServerStatusService.class);
        ServerUrlProvider serverUrlProvider = mock(ServerUrlProvider.class);
        SessionServerController controller = new SessionServerController(
                sessionService, serverStatusService, serverUrlProvider);
        CreateSessionRequest request = new CreateSessionRequest(
                "attempt-1", "session-1", 1L, 2L, SessionType.PVP, null);

        when(serverStatusService.getCurrentState()).thenReturn(ServerState.ACTIVE);
        when(serverUrlProvider.getServerUrl()).thenReturn("https://game.example.com");
        when(sessionService.createSession("attempt-1", request.toSessionDto()))
                .thenReturn(new SessionCreationResult("attempt-1", "session-1", true));

        ResponseEntity<SessionReadyResponse> response = controller.createGameSession(request);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().ready());
        assertEquals("https://game.example.com", response.getBody().serverUrl());
        assertEquals("https://game.example.com/ws", response.getBody().webSocketUrl());
    }
}
