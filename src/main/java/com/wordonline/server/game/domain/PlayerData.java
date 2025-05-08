package com.wordonline.server.game.domain;

import java.util.ArrayList;
import java.util.List;

// This class is used to store player data
public class PlayerData {
    public final static int MAX_CARD_NUM = 6;
    public final static int MAX_HP = 100;

    public int mana = 0;
    public int hp = MAX_HP;
    public List<String> cards = new ArrayList<>();

    // validate and add card
    public boolean addCard(String card) {
        if (MAX_CARD_NUM >= cards.size() + 1) {
            cards.add(card);
            return true;
        }
        return false;
    }

    // validate and use cards
    public boolean useCards(List<String> cards) {
        List<String> tempCards = new ArrayList<>(this.cards);
        for (String card : cards) {
            if (!tempCards.remove(card)) {
                return false;
            }
        }

        for (String card : cards) {
            this.cards.remove(card);
        }

        // TODO - Add mana cost logic

        return true;
    }
}
