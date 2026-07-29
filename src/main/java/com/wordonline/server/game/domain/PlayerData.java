package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.service.ManaCharger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// This class is used to store player data
@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class PlayerData {
    public final static int MAX_CARD_NUM = 6;
    public final static int MAX_HP = 100;

    public final ManaCharger manaCharger;

    private final Parameters parameters;

    public volatile int mana = 0;
    public int hp = MAX_HP;
    public List<CardType> cards = Collections.synchronizedList(new ArrayList<>());

    // charge mana up to max; must stay atomic against card use
    public synchronized void addMana(int delta, int max) {
        mana = Math.min(mana + delta, max);
    }

    // atomic check-and-spend, so two casts cannot both pass the same balance
    public synchronized boolean spendMana(int cost) {
        if (mana < cost) {
            return false;
        }
        mana -= cost;
        return true;
    }

    // validate and add card
    public synchronized boolean addCard(CardType card) {
        if (MAX_CARD_NUM >= cards.size() + 1) {
            cards.add(card);
            return true;
        }
        return false;
    }

    public synchronized boolean validCardsUse(List<CardType> cards) {
        int totalManaCost = 0;
        List<CardType> tempCards = new ArrayList<>(this.cards);
        for (CardType card : cards) {
            totalManaCost += (int) parameters.getValue(card.name().toLowerCase(), "mana_cost");
            if (!tempCards.remove(card)) {
                log.trace("temp cards: {}, trying card {}", tempCards, card);
                return false;
            }
        }
        return totalManaCost <= mana;
    }

    // validate and use cards
    public synchronized boolean useCards(List<CardType> cards) {
        if (!validCardsUse(cards)) {
            return false;
        }

        for (CardType card : cards) {
            this.cards.remove(card);
            mana -= (int) parameters.getValue(card.name().toLowerCase(), "mana_cost");
        }

        return true;
    }
}
