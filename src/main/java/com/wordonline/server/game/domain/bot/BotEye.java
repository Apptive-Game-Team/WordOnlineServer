package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import lombok.Getter;
import java.util.List;

@Getter
public class BotEye {

    private final List<GameObject> gameObjectList;
    private final List<CardType> cardList;
    private final int mana;

    public BotEye(GameSessionData data, Master botSide) {
        var playerData = BotSideUtil.getPlayerData(data, botSide);
        gameObjectList = data.gameObjects;
        cardList = playerData.cards;
        mana = playerData.mana;
    }
}
