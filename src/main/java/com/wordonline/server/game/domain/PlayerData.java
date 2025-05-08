package com.wordonline.server.game.domain;

import java.util.ArrayList;
import java.util.List;

// This class is used to store player data
public class PlayerData {
    public final static int MAX_CARD_NUM = 6;
    public final static int MAX_HP = 100;

    public int mana = 0;
    public int hp = MAX_HP;
    public List<CardType> cards = new ArrayList<>();

    // validate and add card
    public boolean addCard(CardType card) {
        if (MAX_CARD_NUM >= cards.size() + 1) {
            cards.add(card);
            return true;
        }
        return false;
    }

    // validate and use cards
    public boolean useCards(List<CardType> cards) {
        int totalManaCost = 0;
        List<CardType> tempCards = new ArrayList<>(this.cards);
        for (CardType card : cards) {
            totalManaCost += card.getManaCost();
            if (!tempCards.remove(card)) {
                return false;
            }
        }
        if (totalManaCost > mana) {
            return false;
        }

        for (CardType card : cards) {
            this.cards.remove(card);
            mana -= card.getManaCost();
        }

        // TODO: Implement the logic to use the cards

        return true;
    }
}
