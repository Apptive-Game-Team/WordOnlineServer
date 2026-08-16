package com.wordonline.server.session.service;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.session.config.WatchdogProperties;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameLoopWatchdogTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final GameLoopWatchdog watchdog =
            new GameLoopWatchdog(sessionService, new WatchdogProperties(Duration.ofSeconds(10)));

    private final SessionObject sessionObject = mock(SessionObject.class);
    private final GameLoop gameLoop = mock(GameLoop.class);
    private final GameContext gameContext = mock(GameContext.class);

    @BeforeEach
    void setUp() {
        when(sessionService.getSessionObjects()).thenReturn(List.of(sessionObject));
        when(sessionObject.getSessionId()).thenReturn("session-1");
        when(sessionObject.getGameLoop()).thenReturn(gameLoop);
        when(gameLoop.getGameContext()).thenReturn(gameContext);
        when(gameLoop.is_running()).thenReturn(true);
        when(gameLoop.captureLoopThreadStackTrace()).thenReturn(new StackTraceElement[0]);
        when(gameContext.getFrameNum()).thenReturn(123);
    }

    @Test
    void reapsSessionWhoseLastFrameIsOlderThanThreshold() {
        when(gameLoop.getLastFrameEndMillis()).thenReturn(System.currentTimeMillis() - 60_000);

        watchdog.reapStuckSessions();

        verify(sessionService).reapStuckSession(eq(sessionObject), contains("stalled for"));
    }

    @Test
    void leavesTickingSessionAlone() {
        when(gameLoop.getLastFrameEndMillis()).thenReturn(System.currentTimeMillis());

        watchdog.reapStuckSessions();

        verify(sessionService, never()).reapStuckSession(any(), anyString());
    }

    // A loop that already terminated keeps its last frame timestamp forever; the session
    // is on its way out through onLoopTerminated and must not be reaped as stuck.
    @Test
    void ignoresLoopThatIsNoLongerRunning() {
        when(gameLoop.is_running()).thenReturn(false);
        when(gameLoop.getLastFrameEndMillis()).thenReturn(System.currentTimeMillis() - 60_000);

        watchdog.reapStuckSessions();

        verify(sessionService, never()).reapStuckSession(any(), anyString());
    }
}
