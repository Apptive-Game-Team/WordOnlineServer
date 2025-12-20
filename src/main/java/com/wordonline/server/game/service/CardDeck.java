package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.dto.CardInfoDto;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

// CardDeck class to manage the deck of cards
@Slf4j
public class CardDeck {

    private static final float CARD_DRAW_INTERVAL = 1;
    private final Stat cardDrawInterval = new Stat(CARD_DRAW_INTERVAL);
    private final Queue<CardType> cards;

    public CardDeck(List<CardType> cards) {
        Collections.shuffle(cards);
        this.cards = new ConcurrentLinkedDeque<>(cards);
    }

    public void returnCards(List<CardType> cards) {
        this.cards.addAll(cards);
    }

    // random pick card and update to Player Data, Frame Info Dto
    public void drawCard(PlayerData player, CardInfoDto cardInfoDto, int frameNum) {
        if (frameNum % ((int) (GameLoop.FPS * cardDrawInterval.total())) != 0)
            return;
        if (cards.isEmpty() || player.cards.size() >= PlayerData.MAX_CARD_NUM)
            return;

        CardType card = cards.remove();

        if (player.addCard(card)){ // Added
            cardInfoDto.addCard(card);
        }
    }

    // only use for debug
    public void setCards(List<CardType> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
    }

    public void fever() {
        cardDrawInterval.addPercent(-0.5f);
    }
}
