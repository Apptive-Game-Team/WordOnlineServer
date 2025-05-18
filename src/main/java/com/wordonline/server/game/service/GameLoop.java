package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DummyMagicParser;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.dto.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

// GameLoop is the main class that runs the game loop
@Slf4j
public class GameLoop implements Runnable {
    private boolean _running = true;
    public static final int FPS = 1;
    private final SessionObject sessionObject;
    private int _frameNum = 0;
    private final MagicParser magicParser = new DummyMagicParser();

    @Getter
    private final ObjectsInfoDtoBuilder objectsInfoDtoBuilder = new ObjectsInfoDtoBuilder(this);

    public void close() {
        _running = false;
    }

    @Getter
    private GameSessionData gameSessionData = new GameSessionData();

    public GameLoop(SessionObject sessionObject){
        this.sessionObject = sessionObject;
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
        }
    }

    

    public InputResponseDto handleInput(String userId, InputRequestDto inputRequestDto) {
        Master master = sessionObject.getUserSide(userId);
        PlayerData playerData = gameSessionData.getPlayerData(master);

        Magic magic = magicParser.parseMagic(inputRequestDto.getCards());

        boolean valid = playerData.useCards(inputRequestDto.getCards());

        if (magic == null) {
            log.info("{}: {} is not valid", master, inputRequestDto.getCards());
            return new InputResponseDto(false, playerData.mana, inputRequestDto.getId());
        }

        magic.run(this);

        return new InputResponseDto(valid, playerData.mana, inputRequestDto.getId());
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

        gameSessionData.gameObjects.removeAll(toRemove);
    }
}

