package com.wordonline.server.game.service.system;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.service.GameContext;

@Component
@Scope("prototype")
public class FeverTimeSystem implements GameSystem {

    private boolean isFeverTime = false;

    @Override
    public void update(GameContext gameContext) {

        if (!isFeverTime && gameContext.getGameTimer().isFeverTime()) {
            gameContext.getGameSessionData().leftCardDeck.fever();
            gameContext.getGameSessionData().rightCardDeck.fever();
            gameContext.getGameSessionData().leftPlayerData.manaCharger.updateManaCharge(1);
            gameContext.getGameSessionData().rightPlayerData.manaCharger.updateManaCharge(1);
            isFeverTime = true;
        }
    }
}
