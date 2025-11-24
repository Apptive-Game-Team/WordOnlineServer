package com.wordonline.server.game.service;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.*;
import com.wordonline.server.game.dto.frame.SnapshotObjectDto;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.dto.result.ResultType;
import com.wordonline.server.game.util.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// GameLoop is the main class that runs the game loop
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

    @Getter
    protected final GameContext gameContext;

    public final Parameters parameters;

    @Getter
    protected volatile List<SnapshotObjectDto> lastSnapshotObjects = List.of();
    protected volatile int lastSnapshotFrameNum = 0;

    public void init(SessionObject sessionObject, Runnable onTerminated) {
        this.sessionObject = sessionObject;

        this.onTerminated = onTerminated;
        new GameObject(Master.LeftPlayer, PrefabType.Player, GameConfig.LEFT_PLAYER_POSITION, gameContext);
        new GameObject(Master.RightPlayer, PrefabType.Player, GameConfig.RIGHT_PLAYER_POSITION, gameContext);
    }

    public void close() {
        _running = false;
    }

    @Override
    public void run() {
        runLoop();
    }

    // this method is called when the game loop is started
    private void runLoop() {
        long frameDuration = 1000 / FPS;

        while (_running) {
            gameContext.incrementFrameNum();
            long startTime = System.currentTimeMillis();

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
            try { onTerminated.run(); } catch (Exception e) { log.warn("onTerminated failed", e); }
        }
    }

    // this method is called when the game loop is stopped
    abstract void update();

    protected void handleGameEnd() {
        Master loser = gameContext.getResultChecker().getLoser();

        long leftId  = sessionObject.getLeftUserId();
        long rightId = sessionObject.getRightUserId();
        ResultType outcomeLeft = (loser == Master.LeftPlayer)
                ? ResultType.Lose
                : ResultType.Win;

        short leftMmr = mmrService.fetchRating(leftId);
        short rightMmr = mmrService.fetchRating(rightId);
        ResultMmrDto mmrDto = new ResultMmrDto(leftMmr, rightMmr,leftMmr, rightMmr);

        if(sessionObject.getSessionType() == SessionType.PVP)
        {
            mmrDto = mmrService.updateMatchResult(leftId, rightId, outcomeLeft);
        }
        gameContext.getResultChecker().broadcastResult(mmrDto);

        userService.markOnline(leftId);
        userService.markOnline(rightId);

        // 3) 루프 종료
        close();
    }

    // 2) 스냅샷 빌더
    protected void buildSnapshot() {
        lastSnapshotObjects = gameContext.getGameObjects()
                .stream()
                .map(SnapshotMapper::toDto)
                .toList();
        lastSnapshotFrameNum = gameContext.getFrameNum();
    }

    public SnapshotResponseDto getLastSnapshot(Long userId) {
        List<CardType> cards;
        if (gameContext.getSessionObject().getLeftUserId() == userId) {
            cards = gameContext.getGameSessionData().leftPlayerData.cards;
        } else if (gameContext.getSessionObject().getRightUserId() == userId) {
            cards = gameContext.getGameSessionData().rightPlayerData.cards;
        } else {
            cards = List.of();
        }
        return new SnapshotResponseDto(lastSnapshotFrameNum, lastSnapshotObjects, cards);
    }
}

