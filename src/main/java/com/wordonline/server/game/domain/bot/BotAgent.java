package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.service.GameLoop;

public final class BotAgent {

    private final BotHand botHand;
    private final BotBrain botBrain;

    private final SessionObject sessionObject;
    private final GameLoop gameLoop;

    public BotAgent(SessionObject sessionObject) {
        this.botHand = new BotHand();
        this.botBrain = new BotBrain();
        this.sessionObject = sessionObject;
        this.gameLoop = sessionObject.getGameLoop();
    }

    public void onTick(FrameInfoDto myFrame) {
        BotEye botEye = new BotEye(gameLoop.gameSessionData, myFrame);
    }
}
