package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.Master;

import java.util.List;

// What the bot saw at one frame, frozen. Built on the loop thread and handed to the bot executor,
// so the brain never touches a live GameObject, card list or mana counter while a frame is running.
public record BotEye(List<BotVisibleObject> gameObjectList, List<CardType> cardList, int mana) {

    // Must be called on the loop thread.
    public static BotEye observe(GameSessionData data, Master botSide) {
        var playerData = BotSideUtil.getPlayerData(data, botSide);
        return new BotEye(
                data.gameObjects.stream().map(BotVisibleObject::of).toList(),
                List.copyOf(playerData.cards),
                playerData.mana
        );
    }
}
