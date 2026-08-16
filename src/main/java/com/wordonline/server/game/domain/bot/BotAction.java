package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.Master;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotAction {

    public void useCard(SessionObject sessionObject, InputRequestDto inputRequestDto, Master botSide)
    {
        log.debug("[Bot {}] Executing action: {} cards={} target={}", 
                botSide, inputRequestDto.getType(), inputRequestDto.getCards(), inputRequestDto.getPosition());
                
        sessionObject.getGameContext().getMagicInputHandler().handleBotPlayerInput(
                sessionObject.getGameContext(), botSide, inputRequestDto
        );
    }
}
