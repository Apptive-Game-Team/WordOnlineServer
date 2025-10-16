package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import lombok.Getter;
import java.util.List;

@Getter
public class BotEye {

    private final List<GameObject> gameObjectList;
    private final List<CardType> cardList;
    private final int mana;

    public BotEye(GameSessionData data,  FrameInfoDto myFrame) {
        gameObjectList = data.gameObjects;
        cardList = data.rightPlayerData.cards;
        mana = data.rightPlayerData.mana;
    }
}
