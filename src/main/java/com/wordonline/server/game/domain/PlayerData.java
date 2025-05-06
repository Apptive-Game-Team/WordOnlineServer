package com.wordonline.server.game.domain;

import java.util.List;

public class PlayerData {
    public final static int MAX_CARD_NUM = 6;

    public int mana;
    public int hp;
    public List<String> cards;

    // validate and add card
    public boolean addCard(String card) {
        if (MAX_CARD_NUM >= cards.size() + 1) {
            cards.add(card);
            return true;
        }
        return false;
    }

    // validate and use cards
    public boolean useCards(String[] cards) {
        // TODO validate using cards
        return true;
    }

}
