package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DummyMagicParser;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.Collidable;
import com.wordonline.server.game.dto.*;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.dto.frame.ObjectsInfoDto;
import com.wordonline.server.game.util.BruteCollisionChecker;
import com.wordonline.server.game.util.CollisionChecker;
import com.wordonline.server.game.util.Physics;
import com.wordonline.server.game.util.SimplePhysics;

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
    public final MagicParser magicParser = new DummyMagicParser();
    public final ResultChecker resultChecker;

    @Getter
    private final ObjectsInfoDtoBuilder objectsInfoDtoBuilder = new ObjectsInfoDtoBuilder(this);

    public void close() {
        _running = false;
    }

    public final GameSessionData gameSessionData = new GameSessionData();

    public GameLoop(SessionObject sessionObject){
        this.sessionObject = sessionObject;
         resultChecker = new ResultChecker(sessionObject);

         new GameObject(Master.LeftPlayer, PrefabType.Player, new Vector2(1, 5), this);
        new GameObject(Master.RightPlayer, PrefabType.Player, new Vector2(18, 5), this);
    }

    public final Physics physics = new SimplePhysics(gameSessionData.gameObjects);
    public final CollisionChecker collisionChecker = new BruteCollisionChecker();
    public final InputHandler inputHandler = new InputHandler(this);

    public float deltaTime = 1/FPS;

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
        CardInfoDto leftCardInfo = new CardInfoDto();
        CardInfoDto rightCardInfo = new CardInfoDto();

        ObjectsInfoDto objectsInfoDto = objectsInfoDtoBuilder.getObjectsInfoDto();

        FrameInfoDto leftFrameInfoDto = new FrameInfoDto(leftCardInfo, objectsInfoDto);
        FrameInfoDto rightFrameInfoDto = new FrameInfoDto(rightCardInfo, objectsInfoDto);

        gameSessionData.manaCharger.chargeMana(gameSessionData.leftPlayerData, leftFrameInfoDto, _frameNum);
        gameSessionData.manaCharger.chargeMana(gameSessionData.rightPlayerData, rightFrameInfoDto, _frameNum);

        gameSessionData.leftCardDeck.drawCard(gameSessionData.leftPlayerData, leftCardInfo);
        gameSessionData.rightCardDeck.drawCard(gameSessionData.rightPlayerData, rightCardInfo);

        List<GameObject> toRemove = new ArrayList<>();
        gameSessionData.gameObjects.addAll(gameSessionData.gameObjectsToAdd);
        gameSessionData.gameObjectsToAdd.clear();
        for (GameObject gameObject : gameSessionData.gameObjects) {
            if (gameObject.getStatus() == Status.Destroyed) {
                toRemove.add(gameObject);
            } else {
                gameObject.update();
            }
        }
        
        List<GameObject> objects = gameSessionData.gameObjects;
        for (int i = 0; i < objects.size(); i++) {
            GameObject a = objects.get(i);
            if (!(a instanceof Collidable collidableA)) continue;

            for (int j = i + 1; j < objects.size(); j++) {
                GameObject b = objects.get(j);
                if (!(b instanceof Collidable collidableB)) continue;

                if (collisionChecker.isColliding(a, b)) {
                    collidableA.onCollision(b);
                    collidableB.onCollision(a);
                }
            }
        }

        leftFrameInfoDto.setUpdatedMana(gameSessionData.leftPlayerData.mana);
        rightFrameInfoDto.setUpdatedMana(gameSessionData.rightPlayerData.mana);

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
            _running = false;
        }

        gameSessionData.gameObjects.removeAll(toRemove);
    }
}

