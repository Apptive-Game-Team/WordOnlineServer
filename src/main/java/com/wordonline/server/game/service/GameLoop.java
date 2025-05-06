package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.*;
import com.wordonline.server.game.dto.*;

import java.util.List;

// GameLoop is the main class that runs the game loop
public class GameLoop implements Runnable {
    private boolean _running = true;
    public static final int FPS = 10;
    private final SessionObject sessionObject;
    private int _frameNum = 0;

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

    // this method is called when the game loop is stopped
    private void update() {
        CardInfoDto leftCardInfo = new CardInfoDto();
        CardInfoDto rightCardInfo = new CardInfoDto();
        ObjectsInfoDto objectsInfoDto = new ObjectsInfoDto();
        FrameInfoDto leftFrameInfoDto = new FrameInfoDto(leftCardInfo, objectsInfoDto);
        FrameInfoDto rightFrameInfoDto = new FrameInfoDto(rightCardInfo, objectsInfoDto);

        gameSessionData.manaCharger.chargeMana(gameSessionData.leftPlayerData, leftFrameInfoDto, _frameNum);
        gameSessionData.manaCharger.chargeMana(gameSessionData.rightPlayerData, rightFrameInfoDto, _frameNum);

        gameSessionData.leftCardDeck.drawCard(gameSessionData.leftPlayerData, leftCardInfo);
        gameSessionData.rightCardDeck.drawCard(gameSessionData.rightPlayerData, rightCardInfo);

        for (GameObject gameObject : gameSessionData.gameObjects) {
            gameObject.update();
        }

        sessionObject.sendFrameInfo(
            sessionObject.getLeftUserId(),
            leftFrameInfoDto
        );
        sessionObject.sendFrameInfo(
            sessionObject.getRightUserId(),
            rightFrameInfoDto
        );
    }

    // For Test
    // TODO: remove this method
    private FrameInfoDto getTestFrameInfoDto(int frameNum) {
        int mana = (int) (frameNum * 0.1 % 100);
        CardInfoDto cardInfoDto;
        if (frameNum <= 6) {
            cardInfoDto = new CardInfoDto(List.of("Dummy"));
        } else {
            cardInfoDto = new CardInfoDto(List.of());
        }
        ObjectsInfoDto objectsInfoDto = null;


        if (frameNum == 1) {
            CreatedObjectDto createdObjectDto = new CreatedObjectDto(1, "Fireball", new Position(0, 10), Master.LeftPlayer);
            objectsInfoDto = new ObjectsInfoDto(List.of(createdObjectDto), List.of());
        } else if (frameNum > 1) {
            UpdatedObjectDto updatedObjectDto = new UpdatedObjectDto(1, Status.Move, Effect.Burn, new Position(frameNum * 0.5f % 100, 10));
            objectsInfoDto = new ObjectsInfoDto(List.of(), List.of(updatedObjectDto));
        }

        return new FrameInfoDto(mana, cardInfoDto, objectsInfoDto);
    }
}
