package com.wordonline.server.game.service;

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
import com.wordonline.server.game.dto.result.ResultType;
import com.wordonline.server.game.util.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// GameLoop is the main class that runs the game loop
@Slf4j
public class GameLoop implements Runnable {
    private boolean _running = true;
    public static final int FPS = 10;
    public final SessionObject sessionObject;
    private int _frameNum = 0;
    public final MagicParser magicParser = new BasicMagicParser();
    public final ResultChecker resultChecker;
    public final MmrService mmrService;

    @Getter
    private final ObjectsInfoDtoBuilder objectsInfoDtoBuilder = new ObjectsInfoDtoBuilder(this);

    public final GameSessionData gameSessionData;
    public final Physics physics;
    public final CollisionSystem collisionSystem = new BruteCollisionSystem();
    public final InputHandler inputHandler = new InputHandler(this);

    public float deltaTime = 1f / FPS;

    public GameLoop(SessionObject sessionObject, MmrService mmrService) {
        this.sessionObject = sessionObject;
        this.mmrService = mmrService;
        gameSessionData = new GameSessionData(sessionObject.getLeftUserCardDeck(), sessionObject.getRightUserCardDeck());
        resultChecker = new ResultChecker(sessionObject);
        new GameObject(Master.LeftPlayer, PrefabType.Player, new Vector2(1, 5), this);
        new GameObject(Master.RightPlayer, PrefabType.Player, new Vector2(18, 5), this);

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

            update();

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

        List<GameObject> objects = gameSessionData.gameObjects;

        // Handle Collision
        collisionSystem.checkAndHandleCollisions(objects);

        // Mana
        leftFrameInfoDto.setUpdatedMana(gameSessionData.leftPlayerData.mana);
        rightFrameInfoDto.setUpdatedMana(gameSessionData.rightPlayerData.mana);

        // Send Frame Info To Client
        sessionObject.sendFrameInfo(
             sessionObject.getLeftUserId(),
            leftFrameInfoDto
        );
        sessionObject.sendFrameInfo(
            sessionObject.getRightUserId(),
            rightFrameInfoDto
        );

        // Check for game over
        if (resultChecker.checkResult()) {
                Master loser = resultChecker.getLoser();

                long leftId  = sessionObject.getLeftUserId();
                long rightId = sessionObject.getRightUserId();
                ResultType outcomeLeft = (loser == Master.LeftPlayer)
                        ? ResultType.Lose
                        : ResultType.Win;
                mmrService.updateMatchResult(leftId, rightId, outcomeLeft);

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

        // Apply Destroyed GameObject
        gameSessionData.gameObjects.removeAll(toRemove);

        // Apply Added and Removed Component
        for (GameObject gameObject : gameSessionData.gameObjects)
        {
            gameObject.getComponents().addAll(gameObject.getComponentsToAdd()); 
            gameObject.getComponents().removeAll(gameObject.getComponentsToRemove()); 
        }
    }
}

