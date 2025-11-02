package com.wordonline.server.game.service.system;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;

@Component
public class BotAgentSystem implements GameSystem {

    @Override
    public void update(GameContext gameContext) {
        gameContext
                .getGameLoop()
                .getBotAgent()
                .onTick(
                        gameContext.getGameLoop()
                                .getFrameDataSystem()
                                .getRightFrameInfoDto()
                );
    }
}
