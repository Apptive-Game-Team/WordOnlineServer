package com.wordonline.server.game.service;

import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.*;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.dto.frame.ObjectsInfoDto;
import com.wordonline.server.game.dto.frame.SnapshotObjectDto;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.dto.result.ResultType;
import com.wordonline.server.game.service.system.ComponentUpdateSystem;
import com.wordonline.server.game.service.system.FrameDataSystem;
import com.wordonline.server.game.service.system.GameObjectAddRemoteSystem;
import com.wordonline.server.game.service.system.GameObjectStateInitialSystem;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.game.service.system.PhysicSystem;
import com.wordonline.server.game.util.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

// GameLoop is the main class that runs the game loop
@Slf4j
@Scope("prototype")
@Service
@RequiredArgsConstructor
public class GameLoop implements Runnable {
    @Getter
    private boolean _running = true;
    public static final int FPS = 10;
    public SessionObject sessionObject;
    private Runnable onTerminated;

    private final MmrService mmrService;
    private final UserService userService;

    private BotAgent botAgent;

    @Getter
    private final GameContext gameContext;

    private final FrameDataSystem frameDataSystem = new FrameDataSystem();
    private final GameSystem gameObjectStateInitialSystem = new GameObjectStateInitialSystem();
    private final GameSystem componentUpdateSystem = new ComponentUpdateSystem();
    private final GameSystem physicSystem = new PhysicSystem();
    private final GameSystem gameObjectAddRemoveSystem = new GameObjectAddRemoteSystem();

    public final Parameters parameters;

    @Getter
    private volatile SnapshotResponseDto lastSnapshot = new SnapshotResponseDto(0, List.of(), new ArrayList<>());

    // 2) 스냅샷 빌더
    private SnapshotResponseDto buildSnapshot() {
        var list = new ArrayList<SnapshotObjectDto>(gameContext.getGameObjects().size());
        for (var g : gameContext.getGameObjects()) {
            list.add(SnapshotMapper.toDto(g));
        }
        return new SnapshotResponseDto(getGameContext().getFrameNum(), list, gameContext.getGameSessionData().leftPlayerData.cards);
    }

    public void init(SessionObject sessionObject, Runnable onTerminated) {
        this.sessionObject = sessionObject;
        this.onTerminated = onTerminated;
        new GameObject(Master.LeftPlayer, PrefabType.Player, GameConfig.LEFT_PLAYER_POSITION, gameContext);
        new GameObject(Master.RightPlayer, PrefabType.Player, GameConfig.RIGHT_PLAYER_POSITION, gameContext);
        if(sessionObject.getSessionType() == SessionType.Practice)
        {
            botAgent = new BotAgent(sessionObject);
        }

        gameContext.init(sessionObject);
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
    private void update() {
        // Initial DTOs
        frameDataSystem.earlyUpdate(gameContext);

        //bot tick
        if(botAgent != null)
        {
            botAgent.onTick(frameDataSystem.getRightFrameInfoDto());
        }

        // Check for game over
        if (gameContext.getResultChecker().checkResult()) {
            handleGameEnd();
        }

        gameObjectStateInitialSystem.update(gameContext);

        // Run GameObject's Updates
        componentUpdateSystem.update(gameContext);

        physicSystem.update(gameContext);


        gameObjectAddRemoveSystem.update(gameContext);

        frameDataSystem.lateUpdate(gameContext);

        lastSnapshot = buildSnapshot();
    }

    private void handleGameEnd() {
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
}

