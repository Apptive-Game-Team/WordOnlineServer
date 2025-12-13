package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.Master;

public class BotAction {

    public void useCard(SessionObject sessionObject, InputRequestDto inputRequestDto, Master botSide)
    {
        long userId = BotSideUtil.getUserId(sessionObject, botSide);
        
        sessionObject.getGameContext().getMagicInputHandler().handleInput(
                sessionObject.getGameContext(), userId, inputRequestDto
        );
    }
}
