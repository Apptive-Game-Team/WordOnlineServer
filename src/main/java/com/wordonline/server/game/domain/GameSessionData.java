package com.wordonline.server.game.domain;

import com.wordonline.server.game.service.CardDeck;
import com.wordonline.server.game.service.ManaCharger;

import java.util.ArrayList;
import java.util.List;

// this class is used to store the game session data
public class GameSessionData {
    public final PlayerData leftPlayerData = new PlayerData();
    public final PlayerData rightPlayerData = new PlayerData();
    public final List<GameObject> gameObjects = new ArrayList<>();

    public final CardDeck leftCardDeck = new CardDeck();
    public final CardDeck rightCardDeck = new CardDeck();
    public final ManaCharger manaCharger = new ManaCharger();
}
