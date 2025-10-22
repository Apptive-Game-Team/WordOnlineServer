package com.wordonline.server.game.service.system;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.dto.CardInfoDto;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.dto.frame.ObjectsInfoDto;
import com.wordonline.server.game.service.GameContext;

import lombok.Getter;

@Getter
@Component
@Scope("prototype")
public class FrameDataSystem implements EarlyUpdateSystem, LateUpdateSystem {

    private FrameInfoDto leftFrameInfoDto;
    private FrameInfoDto rightFrameInfoDto;

    @Override
    public void earlyUpdate(GameContext gameContext) {
        CardInfoDto leftCardInfo = new CardInfoDto();
        CardInfoDto rightCardInfo = new CardInfoDto();

        ObjectsInfoDto objectsInfoDto = gameContext.getObjectsInfoDto();

        leftFrameInfoDto = new FrameInfoDto(leftCardInfo, objectsInfoDto, gameContext.getGameSessionData());
        rightFrameInfoDto = new FrameInfoDto(rightCardInfo, objectsInfoDto, gameContext.getGameSessionData());

        // Charge Mana
        gameContext.getGameSessionData().leftPlayerData.manaCharger.chargeMana(gameContext.getGameSessionData().leftPlayerData, leftFrameInfoDto, gameContext.getFrameNum());
        gameContext.getGameSessionData().rightPlayerData.manaCharger.chargeMana(gameContext.getGameSessionData().rightPlayerData, rightFrameInfoDto, gameContext.getFrameNum());
        // Mana
        leftFrameInfoDto.setUpdatedMana(gameContext.getGameSessionData().leftPlayerData.mana);
        rightFrameInfoDto.setUpdatedMana(gameContext.getGameSessionData().rightPlayerData.mana);

        // Draw Cards
        gameContext.getGameSessionData().leftCardDeck.drawCard(gameContext.getGameSessionData().leftPlayerData, leftCardInfo);
        gameContext.getGameSessionData().rightCardDeck.drawCard(gameContext.getGameSessionData().rightPlayerData, rightCardInfo);
    }

    @Override
    public void lateUpdate(GameContext gameContext) {

        // Send Frame Info To Client
        gameContext.getSessionObject().sendFrameInfo(
                gameContext.getSessionObject().getLeftUserId(),
                leftFrameInfoDto
        );
        gameContext.getSessionObject().sendFrameInfo(
                gameContext.getSessionObject().getRightUserId(),
                rightFrameInfoDto
        );
    }
}
