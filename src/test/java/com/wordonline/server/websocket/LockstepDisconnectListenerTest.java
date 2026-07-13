package com.wordonline.server.websocket;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.InputRelayLoop;
import com.wordonline.server.session.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockstepDisconnectListenerTest {
    @Test
    void abortsUsersActiveRelaySession() {
        SessionService sessions = mock(SessionService.class);
        SessionObject session = mock(SessionObject.class);
        InputRelayLoop loop = mock(InputRelayLoop.class);
        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        PrincipalDetails principal = new PrincipalDetails(10L, List.of());
        when(event.getUser()).thenReturn(principal);
        when(sessions.findByUserId(10L)).thenReturn(Optional.of(session));
        when(session.getGameLoop()).thenReturn(loop);

        new LockstepDisconnectListener(sessions).disconnected(event);

        verify(loop).disconnected(10L);
    }
}
