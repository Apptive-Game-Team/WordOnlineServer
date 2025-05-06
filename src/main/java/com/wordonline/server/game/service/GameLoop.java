package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.FrameInfo;
import com.wordonline.server.game.domain.Position;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.*;

import java.util.ArrayList;
import java.util.List;

public class GameLoop implements Runnable {
    private boolean _running = true;
    private final int FPS = 10;
    private final SessionObject sessionObject;
    private int _frameNum = 0;

    public GameLoop(SessionObject sessionObject){
        this.sessionObject = sessionObject;
    }

    @Override
    public void run() {
        runLoop();
    }

    private void runLoop() {
        long frameDuration = 1000 / FPS;

        while (_running) {
            _frameNum++;
            long startTime = System.currentTimeMillis();

            FrameInfo frameInfo = update();
            sessionObject.broadcastFrameInfo(getTestFrameInfoDto(_frameNum));

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

    private FrameInfo update() {
        // TODO
        return null;
    }

    // For Test
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
