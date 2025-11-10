package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.CardDeck;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

// this class is used to store the game session data
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class GameSessionData {
    public final PlayerData leftPlayerData;
    public final PlayerData rightPlayerData;
    public final List<GameObject> gameObjects = new ArrayList<>();
    public final List<GameObject> gameObjectsToAdd = new ArrayList<>();

    public CardDeck leftCardDeck;
    public CardDeck rightCardDeck;

    public void addGameObject(GameObject gameObject) {
        gameObjectsToAdd.add(gameObject);
    }

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

    public void initCardDeck(CardDeck leftCardDeck, CardDeck rightCardDeck) {
        this.leftCardDeck = leftCardDeck;
        this.rightCardDeck = rightCardDeck;
    }
}
