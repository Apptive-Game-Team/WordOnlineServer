package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.FrameInfo;
import com.wordonline.server.game.domain.SessionObject;

public class GameLoop implements Runnable {
    private boolean _running = true;
    private final int FPS = 10;
    private final SessionObject sessionObject;

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
            long startTime = System.currentTimeMillis();

            FrameInfo frameInfo = update();
            sessionObject.broadcastFrameInfo(frameInfo);

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
}
