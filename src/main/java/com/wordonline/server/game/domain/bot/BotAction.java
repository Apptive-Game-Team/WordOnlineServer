package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.Master;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotAction {

    public void useCard(SessionObject sessionObject, InputRequestDto inputRequestDto, Master botSide) {
        long userId = BotSideUtil.getUserId(sessionObject, botSide);
        int frameNum = sessionObject.getGameContext().getFrameNum();

        log.info("[Bot {}] Submitting input: cards={} target={}",
                botSide, inputRequestDto.getCards(), inputRequestDto.getPosition());

        sessionObject.getGameContext().getInputBufferSystem().receive(frameNum, userId, inputRequestDto);
    }
}
