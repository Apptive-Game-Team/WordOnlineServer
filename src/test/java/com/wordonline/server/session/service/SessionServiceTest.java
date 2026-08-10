package com.wordonline.server.session.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.PingChecker;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.ResultChecker;
import com.wordonline.server.game.service.UserScenarioService;
import com.wordonline.server.game.service.UserService;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.util.GameLoopFactory;
import com.wordonline.server.session.util.SessionObjectFactory;
import com.wordonline.server.statistic.service.StatisticService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
    private SessionObject sessionObject;
    private GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        sessionService.clearSessions();
        sessionDto = new SessionDto("session-1", 1L, 2L, SessionType.PVP, null);
        sessionObject = mock(SessionObject.class);
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

    @Test
    void reportsInactiveForUnknownSessionId() {
        SessionService service = new SessionService(null, null, null, null, null);

        assertThat(service.isSessionActive("no-such-session")).isFalse();
    }

    @Test
    void hardenedTeardownStillPublishesDecreasedSessionCount() {
        List<Integer> publishedCounts = new CopyOnWriteArrayList<>();
        sessionService.subscribeSessionNumChange(recordingSubscriber(publishedCounts));

        ArgumentCaptor<Runnable> onTerminated = ArgumentCaptor.forClass(Runnable.class);
        sessionService.createSession("attempt-1", sessionDto);
        verify(gameLoop).init(eq(sessionObject), onTerminated.capture());
        await().atMost(Duration.ofSeconds(5)).until(() -> publishedCounts.contains(1));

        stubTeardownCollaborators();
        onTerminated.getValue().run();

        await().atMost(Duration.ofSeconds(5)).until(() -> publishedCounts.contains(0));
        assertFalse(sessionService.isSessionActive(sessionDto.sessionId()));
    }

    @Test
    void teardownPublishesDecreasedSessionCountEvenWhenStatisticsSaveFails() {
        List<Integer> publishedCounts = new CopyOnWriteArrayList<>();
        sessionService.subscribeSessionNumChange(recordingSubscriber(publishedCounts));

        ArgumentCaptor<Runnable> onTerminated = ArgumentCaptor.forClass(Runnable.class);
        sessionService.createSession("attempt-1", sessionDto);
        verify(gameLoop).init(eq(sessionObject), onTerminated.capture());

        stubTeardownCollaborators();
        doThrow(new IllegalStateException("statistics unavailable"))
                .when(statisticService).saveGameResult(any(), any(), any());

        assertDoesNotThrow(() -> onTerminated.getValue().run());

        await().atMost(Duration.ofSeconds(5)).until(() -> publishedCounts.contains(0));
    }

    private ResultChecker stubTeardownCollaborators() {
        GameContext gameContext = mock(GameContext.class);
        ResultChecker resultChecker = mock(ResultChecker.class);

        when(sessionObject.getPingChecker()).thenReturn(mock(PingChecker.class));
        when(sessionObject.getGameContext()).thenReturn(gameContext);
        when(gameContext.getResultChecker()).thenReturn(resultChecker);
        when(resultChecker.getLoser()).thenReturn(null);

        return resultChecker;
    }

    private Flow.Subscriber<Integer> recordingSubscriber(List<Integer> sink) {
        return new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                sink.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        };
    }
}
