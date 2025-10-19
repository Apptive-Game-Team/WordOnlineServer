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
    private int _frameNum = 0;

    public final MmrService mmrService;
    private final UserService userService;

    private BotAgent botAgent;

    @Getter
    private final GameContext gameContext;

    private final PhysicSystem physicSystem = new PhysicSystem();

    public final Parameters parameters;

    @Getter
    private volatile SnapshotResponseDto lastSnapshot = new SnapshotResponseDto(0, List.of(), new ArrayList<>());

    // 2) 스냅샷 빌더
    private SnapshotResponseDto buildSnapshot() {
        var list = new ArrayList<SnapshotObjectDto>(gameContext.getGameObjects().size());
        for (var g : gameContext.getGameObjects()) {
            list.add(SnapshotMapper.toDto(g));
        }
        return new SnapshotResponseDto(_frameNum, list, gameContext.getGameSessionData().leftPlayerData.cards);
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
            _frameNum++;
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
        CardInfoDto leftCardInfo = new CardInfoDto();
        CardInfoDto rightCardInfo = new CardInfoDto();

        ObjectsInfoDto objectsInfoDto = gameContext.getObjectsInfoDto();

        FrameInfoDto leftFrameInfoDto = new FrameInfoDto(leftCardInfo, objectsInfoDto, gameContext.getGameSessionData());
        FrameInfoDto rightFrameInfoDto = new FrameInfoDto(rightCardInfo, objectsInfoDto, gameContext.getGameSessionData());

        // Charge Mana
        gameContext.getGameSessionData().leftPlayerData.manaCharger.chargeMana(gameContext.getGameSessionData().leftPlayerData, leftFrameInfoDto, _frameNum);
        gameContext.getGameSessionData().rightPlayerData.manaCharger.chargeMana(gameContext.getGameSessionData().rightPlayerData, rightFrameInfoDto, _frameNum);

        // Draw Cards
        gameContext.getGameSessionData().leftCardDeck.drawCard(gameContext.getGameSessionData().leftPlayerData, leftCardInfo);
        gameContext.getGameSessionData().rightCardDeck.drawCard(gameContext.getGameSessionData().rightPlayerData, rightCardInfo);

        List<GameObject> toRemove = new ArrayList<>();

        // Mana
        leftFrameInfoDto.setUpdatedMana(gameContext.getGameSessionData().leftPlayerData.mana);
        rightFrameInfoDto.setUpdatedMana(gameContext.getGameSessionData().rightPlayerData.mana);

        //bot tick
        if(botAgent != null)
        {
            botAgent.onTick(rightFrameInfoDto);
        }

        // Check for game over
        if (gameContext.getResultChecker().checkResult()) {
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
            getGameContext().getResultChecker().broadcastResult(mmrDto);

            userService.markOnline(leftId);
            userService.markOnline(rightId);
            // 3) 루프 종료
            close();
        }


        //Apply Idle Status to Non-Idle Object
        getGameContext().getGameObjects()
                .stream()
                .filter(gameObject -> gameObject.getStatus() != Status.Idle)
                .forEach(gameObject -> gameObject.setStatus(Status.Idle));

        // Apply Created GameObject
        getGameContext().getGameObjects().addAll(getGameContext().getGameSessionData().gameObjectsToAdd);
        getGameContext().getGameSessionData().gameObjectsToAdd.clear();

        // Run GameObject's Updates
        for (GameObject gameObject : getGameContext().getGameObjects()) {
            if (gameObject.getStatus() == Status.Destroyed) {
                toRemove.add(gameObject);
            } else {
                gameObject.update();
            }
        }
        physicSystem.handleCollisions(getGameContext().getGameObjects());

        List<GameObject> objects = getGameContext().getGameObjects();

        // Handle Collision
        physicSystem.checkAndHandleCollisions(objects);

        physicSystem.onUpdateEnd(getGameContext().getGameObjects());

        // Send Frame Info To Client
        sessionObject.sendFrameInfo(
                sessionObject.getLeftUserId(),
                leftFrameInfoDto
        );
        sessionObject.sendFrameInfo(
                sessionObject.getRightUserId(),
                rightFrameInfoDto
        );



        // Apply Destroyed GameObject
        getGameContext().getGameObjects().removeAll(toRemove);


        // Apply Added and Removed Component
        for (GameObject go : getGameContext().getGameObjects()) {

            if (!go.getComponentsToAdd().isEmpty()) {
                List<Component> toAdd = new ArrayList<>(go.getComponentsToAdd());
                go.getComponentsToAdd().clear();
                go.getComponents().addAll(toAdd);
                for (Component c : toAdd) {
                    c.start();
                }
            }

            if (!go.getComponentsToRemove().isEmpty()) {
                List<Component> toRem = new ArrayList<>(go.getComponentsToRemove());
                go.getComponentsToRemove().clear();
                for (Component c : toRem) {
                    c.onDestroy();
                }
                go.getComponents().removeAll(toRem);
            }
        }
        lastSnapshot = buildSnapshot();
    }
}

