package com.wordonline.server.session.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.session.config.WatchdogProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Detects game loops that stopped ticking and reaps their sessions.
 *
 * <p>Every in-loop safeguard (the match timer, the crash handler) runs on the loop thread
 * itself, so a deadlocked or runaway frame disables all of them at once while the session
 * keeps reporting itself as alive. This is the one check that runs on another thread: it
 * watches {@link GameLoop#getLastFrameEndMillis()}, the only signal that stops moving when
 * the loop does, and hands stalled sessions to
 * {@link SessionService#reapStuckSession(SessionObject, String)} together with a snapshot
 * of where the loop thread was blocked.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameLoopWatchdog {

    private final SessionService sessionService;
    private final WatchdogProperties watchdogProperties;

    @Scheduled(fixedDelayString = "${watchdog.check-interval-ms:5000}")
    public void reapStuckSessions() {
        long thresholdMillis = watchdogProperties.stuckThreshold().toMillis();

        for (SessionObject sessionObject : sessionService.getSessionObjects()) {
            GameLoop loop = sessionObject.getGameLoop();
            if (loop == null || !loop.is_running()) {
                continue;
            }
            long stallMillis = System.currentTimeMillis() - loop.getLastFrameEndMillis();
            if (stallMillis < thresholdMillis) {
                continue;
            }

            String stackTrace = formatStackTrace(loop.captureLoopThreadStackTrace());
            log.error("[Watchdog] Loop stalled for {} ms; sessionId: {}, frame: {}, loop thread at:\n{}",
                    stallMillis,
                    sessionObject.getSessionId(),
                    loop.getGameContext().getFrameNum(),
                    stackTrace.isEmpty() ? "(thread gone)" : stackTrace);

            String endDetail = "stalled for " + stallMillis + " ms at frame "
                    + loop.getGameContext().getFrameNum()
                    + (stackTrace.isEmpty() ? "" : "\n" + stackTrace);
            sessionService.reapStuckSession(sessionObject, endDetail);
        }
    }

    private String formatStackTrace(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace)
                .map(element -> "at " + element)
                .collect(Collectors.joining("\n"));
    }
}
