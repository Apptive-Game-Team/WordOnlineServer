package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.CardDeck;
import com.wordonline.server.game.service.ManaCharger;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// this class is used to store the game session data
public class GameSessionData {
    public final PlayerData leftPlayerData;
    public final PlayerData rightPlayerData;
    public final List<GameObject> gameObjects = new ArrayList<>();
    public final List<GameObject> gameObjectsToAdd = new ArrayList<>();

    public final CardDeck leftCardDeck;
    public final CardDeck rightCardDeck;


    public PlayerData getPlayerData(Master master){
        if (master == Master.LeftPlayer) {
            return leftPlayerData;
        } else if (master == Master.RightPlayer) {
            return rightPlayerData;
        } else {
            return null;
        }
    }

    public CardDeck getCardDeck(Master master){
        if (master == Master.LeftPlayer) {
            return leftCardDeck;
        } else if (master == Master.RightPlayer) {
            return rightCardDeck;
        } else {
            return null;
        }
    }

    public GameSessionData(CardDeck leftCardDeck, CardDeck rightCardDeck, Parameters parameters) {
        this.leftCardDeck = leftCardDeck;
        this.rightCardDeck = rightCardDeck;

        leftPlayerData = new PlayerData(parameters);
        rightPlayerData = new PlayerData(parameters);
    }
}
