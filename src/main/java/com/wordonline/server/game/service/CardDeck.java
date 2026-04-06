package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.magic.CardType;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class CardDeck {

    private final Stat cardDrawInterval = new Stat(1f);
    private final Queue<CardType> cards;

    public CardDeck(List<CardType> cards) {
        Collections.shuffle(cards);
        this.cards = new ConcurrentLinkedDeque<>(cards);
    }

    public void returnCards(List<CardType> cards) {
        this.cards.addAll(cards);
    }

    public void setCards(List<CardType> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
    }

    /** Returns all cards in the deck without removing them. Used for lockstep SessionStartDto. */
    public List<CardType> peekAll() {
        return List.copyOf(cards);
    }

    public void fever() {
        cardDrawInterval.addPercent(-0.5f);
    }
}
