package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.dto.result.ResultType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class GameLoop implements Runnable {
    @Getter
    private boolean _running = true;
    public static final int FPS = 20;
    public SessionObject sessionObject;
    private Runnable onTerminated;

    private final MmrService mmrService;
    private final UserService userService;

    /** Override in subclasses that support bots. */
    public BotAgent getLeftBotAgent() { return null; }
    /** Override in subclasses that support bots. */
    public BotAgent getRightBotAgent() { return null; }

    protected MmrService getMmrService() { return mmrService; }
    protected UserService getUserService() { return userService; }

    @Getter
    protected final GameContext gameContext;

    public final Parameters parameters;

    public void init(SessionObject sessionObject, Runnable onTerminated) {
        initializeLoop(sessionObject, onTerminated);
    }

    protected final void initializeLoop(SessionObject sessionObject, Runnable onTerminated) {
        this.sessionObject = sessionObject;
        this.onTerminated = onTerminated;
    }

    public void close() {
        _running = false;
    }

    @Override
    public void run() {
        runLoop();
    }

    private static final long MAX_INPUT_WAIT_MS = 100;

    private void runLoop() {
        long frameDuration = 1000 / FPS;

        while (_running) {
            gameContext.incrementFrameNum();
            long startTime = System.currentTimeMillis();

            waitForInputs(startTime);

            try {
                update();
            } catch (Exception e) {
                log.error("[ERROR] {}", e.getMessage(), e);
            }

            long endTime = System.currentTimeMillis();
            long sleepTime = frameDuration - (endTime - startTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
            gameContext.setDeltaTime((System.currentTimeMillis() - startTime) / 1000.0f);
        }

        if (onTerminated != null) {
            try {
                onTerminated.run();
            } catch (Exception e) {
                log.warn("onTerminated failed", e);
            }
        }
    }

    private void waitForInputs(long frameStartMs) {
        var playerIds = java.util.List.of(
                sessionObject.getLeftUserId(),
                sessionObject.getRightUserId()
        );

        while (!gameContext.getInputBufferSystem().isReady(
                gameContext.getFrameNum(), playerIds, frameStartMs, MAX_INPUT_WAIT_MS)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    abstract void update();

    protected void handleGameEnd() {
        Master loser = gameContext.getResultChecker().getLoser();

        long leftId = sessionObject.getLeftUserId();
        long rightId = sessionObject.getRightUserId();
        ResultType outcomeLeft = (loser == Master.LeftPlayer)
                ? ResultType.Lose
                : ResultType.Win;

        short leftMmr = mmrService.fetchRating(leftId);
        short rightMmr = mmrService.fetchRating(rightId);
        ResultMmrDto mmrDto = new ResultMmrDto(leftMmr, rightMmr, leftMmr, rightMmr);

        if (sessionObject.getSessionType() == SessionType.PVP) {
            mmrDto = mmrService.updateMatchResult(leftId, rightId, outcomeLeft);
        }
        gameContext.getResultChecker().broadcastResult(mmrDto);

        userService.markOnline(leftId);
        userService.markOnline(rightId);

        close();
    }
}
