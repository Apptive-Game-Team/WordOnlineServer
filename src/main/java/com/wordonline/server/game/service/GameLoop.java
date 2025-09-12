package com.wordonline.server.game.service;

import com.wordonline.server.auth.domain.User;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.domain.magic.parser.BasicMagicParser;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
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
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// GameLoop is the main class that runs the game loop
@Slf4j
public class GameLoop implements Runnable {
    @Getter
    private boolean _running = true;
    public static final int FPS = 10;
    public final SessionObject sessionObject;
    private int _frameNum = 0;
    public final MagicParser magicParser = new BasicMagicParser();
    public final ResultChecker resultChecker;
    public final MmrService mmrService;
    private final UserService userService;

    @Getter
    private final ObjectsInfoDtoBuilder objectsInfoDtoBuilder = new ObjectsInfoDtoBuilder(this);

    private final PhysicSystem physicSystem = new PhysicSystem();
    public final GameSessionData gameSessionData;
    public final Physics physics;
    public final InputHandler inputHandler = new InputHandler(this);
    public final Parameters parameters;

    public float deltaTime = 1f / FPS;

    @Getter
    private volatile SnapshotResponseDto lastSnapshot = new SnapshotResponseDto(0, List.of());

    // 2) 스냅샷 빌더
    private SnapshotResponseDto buildSnapshot() {
        var list = new ArrayList<SnapshotObjectDto>(gameSessionData.gameObjects.size());
        for (var g : gameSessionData.gameObjects) {
            list.add(SnapshotMapper.toDto(g));
        }
        return new SnapshotResponseDto(_frameNum, list);
    }

    public GameLoop(SessionObject sessionObject, MmrService mmrService, UserService userService, Parameters parameters) {
        this.sessionObject = sessionObject;
        this.mmrService = mmrService;
        this.userService   = userService;
        gameSessionData = new GameSessionData(sessionObject.getLeftUserCardDeck(), sessionObject.getRightUserCardDeck());
        resultChecker = new ResultChecker(sessionObject);
        new GameObject(Master.LeftPlayer, PrefabType.Player, GameConfig.LEFT_PLAYER_POSITION, this);
        new GameObject(Master.RightPlayer, PrefabType.Player, GameConfig.RIGHT_PLAYER_POSITION, this);

        this.parameters = parameters;
        physics = new SimplePhysics(gameSessionData.gameObjects);
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
            deltaTime = (System.currentTimeMillis() - startTime) / 1000.0f;
        }
    }

    // this method is called when the game loop is stopped
    private void update() {
        // Initial DTOs
        CardInfoDto leftCardInfo = new CardInfoDto();
        CardInfoDto rightCardInfo = new CardInfoDto();

        ObjectsInfoDto objectsInfoDto = objectsInfoDtoBuilder.getObjectsInfoDto();

        FrameInfoDto leftFrameInfoDto = new FrameInfoDto(leftCardInfo, objectsInfoDto, gameSessionData);
        FrameInfoDto rightFrameInfoDto = new FrameInfoDto(rightCardInfo, objectsInfoDto, gameSessionData);

        // Charge Mana
        gameSessionData.manaCharger.chargeMana(gameSessionData.leftPlayerData, leftFrameInfoDto, _frameNum);
        gameSessionData.manaCharger.chargeMana(gameSessionData.rightPlayerData, rightFrameInfoDto, _frameNum);

        // Draw Cards
        gameSessionData.leftCardDeck.drawCard(gameSessionData.leftPlayerData, leftCardInfo);
        gameSessionData.rightCardDeck.drawCard(gameSessionData.rightPlayerData, rightCardInfo);

        List<GameObject> toRemove = new ArrayList<>();

        // Mana
        leftFrameInfoDto.setUpdatedMana(gameSessionData.leftPlayerData.mana);
        rightFrameInfoDto.setUpdatedMana(gameSessionData.rightPlayerData.mana);

        // Check for game over
        if (resultChecker.checkResult()) {
                Master loser = resultChecker.getLoser();

                long leftId  = sessionObject.getLeftUserId();
                long rightId = sessionObject.getRightUserId();
                ResultType outcomeLeft = (loser == Master.LeftPlayer)
                        ? ResultType.Lose
                        : ResultType.Win;

                ResultMmrDto mmrDto = mmrService.updateMatchResult(leftId, rightId, outcomeLeft);
                resultChecker.broadcastResult(mmrDto);
                userService.markOnline(leftId);
                userService.markOnline(rightId);

                // 3) 루프 종료
                close();
        }

        // Apply Created GameObject
        gameSessionData.gameObjects.addAll(gameSessionData.gameObjectsToAdd);
        gameSessionData.gameObjectsToAdd.clear();

        // Run GameObject's Updates
        for (GameObject gameObject : gameSessionData.gameObjects) {
            if (gameObject.getStatus() == Status.Destroyed) {
                toRemove.add(gameObject);
            } else {
                gameObject.update();
            }
        }
        physicSystem.handleCollisions(gameSessionData.gameObjects);

        List<GameObject> objects = gameSessionData.gameObjects;

        // Handle Collision
        physicSystem.checkAndHandleCollisions(objects);

        physicSystem.onUpdateEnd(gameSessionData.gameObjects);

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
        gameSessionData.gameObjects.removeAll(toRemove);

        // Apply Added and Removed Component
        for (GameObject gameObject : gameSessionData.gameObjects)
        {
            gameObject.getComponents().addAll(gameObject.getComponentsToAdd());
            for (Component component : gameObject.getComponentsToAdd())
            {
               component.start();
            }
            gameObject.getComponentsToAdd().clear();

            gameObject.getComponents().removeAll(gameObject.getComponentsToRemove());
            for (Component component : gameObject.getComponentsToRemove())
            {
                component.onDestroy();
            }
            gameObject.getComponentsToRemove().clear();
        }
        lastSnapshot = buildSnapshot();
    }

}

