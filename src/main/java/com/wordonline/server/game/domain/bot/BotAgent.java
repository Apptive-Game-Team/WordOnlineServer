package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.service.GameLoop;

import java.util.concurrent.atomic.AtomicInteger;

public final class BotAgent {

    private final BotAction botAction;
    private final BotBrain botBrain;

    private final SessionObject sessionObject;
    private final GameLoop gameLoop;

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    public BotAgent(SessionObject sessionObject) {
        this.botAction = new BotAction();
        this.botBrain = new BotBrain();
        this.sessionObject = sessionObject;
        this.gameLoop = sessionObject.getGameLoop();
    }

    public void onTick(FrameInfoDto myFrame) {
        BotEye botEye = new BotEye(gameLoop.gameSessionData, myFrame);
        BotBrain.InputDecision  decision = botBrain.think(
                botEye.getGameObjectList(),
                botEye.getCardList(),
                gameLoop,
                botEye.getMana());
        if(decision != null)
        {
            InputRequestDto inputRequestDto = new InputRequestDto();
            inputRequestDto.setType("useMagic");
            inputRequestDto.setId(NEXT_ID.getAndIncrement());
            inputRequestDto.setCards(decision.playCards());
            inputRequestDto.setPosition(decision.target().toVector2());
            botAction.useCard(sessionObject, inputRequestDto);
        }
    }
}
