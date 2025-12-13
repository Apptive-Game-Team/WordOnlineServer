package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.Master;

public class BotAction {

    public void useCard(SessionObject sessionObject, InputRequestDto inputRequestDto, Master botSide)
    {
        long userId = (botSide == Master.LeftPlayer) 
                ? sessionObject.getLeftUserId() 
                : sessionObject.getRightUserId();
        
        sessionObject.getGameContext().getMagicInputHandler().handleInput(
                sessionObject.getGameContext(), userId, inputRequestDto
        );
    }
}
