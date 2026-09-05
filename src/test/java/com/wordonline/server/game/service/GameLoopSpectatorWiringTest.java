package com.wordonline.server.game.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.websocket.SpectatorSubscriptionRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * initializeLoop is the one path every loop takes, PVP and PVE alike, so it is where the session
 * is handed the subscription registry. Without the wiring the session would answer "no spectators"
 * forever and nobody watching would ever see a frame.
 */
class GameLoopSpectatorWiringTest {

    private static final String SESSION_ID = "session-1";
    private static final String BROADCAST_DESTINATION = "/game/session-1/frameInfos/0";

    private final SpectatorSubscriptionRegistry registry = mock(SpectatorSubscriptionRegistry.class);

    @Test
    void handsTheSubscriptionRegistryToTheSessionOnInit() {
        SessionObject sessionObject = sessionObject();
        GameLoop loop = loop();
        loop.setSpectatorSubscriptionRegistry(registry);

        loop.init(sessionObject, () -> {
        });

        assertThat(sessionObject.hasSpectators()).isFalse();

        when(registry.hasSubscribers(BROADCAST_DESTINATION)).thenReturn(true);

        assertThat(sessionObject.hasSpectators()).isTrue();
    }

    private SessionObject sessionObject() {
        return new SessionObject(SESSION_ID, 11L, 22L, mock(SimpMessagingTemplate.class), List.of(), List.of());
    }

    private GameLoop loop() {
        return new GameLoop(mock(MmrService.class), mock(UserService.class),
                mock(GameContext.class), mock(Parameters.class)) {
            @Override
            protected void update() {
            }
        };
    }
}
