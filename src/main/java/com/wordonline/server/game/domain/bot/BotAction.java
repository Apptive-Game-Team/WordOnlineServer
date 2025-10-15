package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.InputRequestDto;

public class BotAction {

    public void useCard(SessionObject sessionObject, InputRequestDto inputRequestDto)
    {
        sessionObject.getGameLoop().magicInputHandler.handleInput(
                sessionObject.getGameLoop(), -1, inputRequestDto
        );
    }
}
