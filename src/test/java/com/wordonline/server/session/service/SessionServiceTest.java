package com.wordonline.server.session.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.UserScenarioService;
import com.wordonline.server.game.service.UserService;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.util.GameLoopFactory;
import com.wordonline.server.session.util.SessionObjectFactory;
import com.wordonline.server.statistic.service.StatisticService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    private final SessionObjectFactory sessionObjectFactory = mock(SessionObjectFactory.class);
    private final GameLoopFactory gameLoopFactory = mock(GameLoopFactory.class);
    private final StatisticService statisticService = mock(StatisticService.class);
    private final SessionService sessionService = new SessionService(
            sessionObjectFactory,
            gameLoopFactory,
            statisticService,
            mock(UserService.class),
            mock(UserScenarioService.class));

    private SessionDto sessionDto;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        sessionService.clearSessions();
        sessionDto = new SessionDto("session-1", 1L, 2L, SessionType.PVP, null);
        SessionObject sessionObject = mock(SessionObject.class);
        gameLoop = mock(GameLoop.class);

        when(sessionObjectFactory.createSessionObject(sessionDto)).thenReturn(sessionObject);
        when(sessionObject.getSessionId()).thenReturn(sessionDto.sessionId());
        when(sessionObject.getSessionType()).thenReturn(sessionDto.sessionType());
        when(sessionObject.getGameLoop()).thenReturn(gameLoop);
        when(gameLoopFactory.create(sessionDto.sessionType())).thenReturn(gameLoop);
        when(gameLoop.is_running()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        sessionService.clearSessions();
    }

    @Test
    void repeatedAttemptReturnsReadySessionWithoutCreatingDuplicate() {
        SessionCreationResult first = sessionService.createSession("attempt-1", sessionDto);
        SessionCreationResult repeated = sessionService.createSession("attempt-1", sessionDto);

        assertTrue(first.ready());
        assertTrue(repeated.ready());
        verify(sessionObjectFactory, times(1)).createSessionObject(sessionDto);
    }

    @Test
    void repeatedAttemptRejectsDifferentSession() {
        sessionService.createSession("attempt-1", sessionDto);
        SessionDto conflicting = new SessionDto("session-2", 1L, 2L, SessionType.PVP, null);

        assertThrows(IllegalArgumentException.class,
                () -> sessionService.createSession("attempt-1", conflicting));
    }

    @Test
    void missingSessionIsInactive() {
        assertFalse(sessionService.isSessionActive("missing"));
    }
}
