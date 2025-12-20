package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.service.GameLoop;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public final class BotAgent {

    private final BotAction botAction;
    private final BotBrain botBrain;

    private final SessionObject sessionObject;
    private final GameLoop gameLoop;
    private final Master botSide;

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    public BotAgent(SessionObject sessionObject, MagicParser magicParser, Master botSide) {
        this.botAction = new BotAction();
        this.botBrain = new BotBrain(magicParser);
        this.sessionObject = sessionObject;
        this.gameLoop = sessionObject.getGameLoop();
        this.botSide = botSide;
    }

    public void onTick(FrameInfoDto myFrame) {
        BotEye botEye = new BotEye(gameLoop.getGameContext().getGameSessionData(), myFrame, botSide);
        BotBrain.InputDecision decision = botBrain.think(
                botEye.getGameObjectList(),
                botEye.getCardList(),
                gameLoop,
                botEye.getMana(),
                botSide);
        if(decision != null)
        {
            InputRequestDto inputRequestDto = new InputRequestDto();
            inputRequestDto.setType("useMagic");
            inputRequestDto.setId(NEXT_ID.getAndIncrement());
            inputRequestDto.setCards(decision.playCards());
            inputRequestDto.setPosition(decision.target());
            botAction.useCard(sessionObject, inputRequestDto, botSide);
        }
    }
}
