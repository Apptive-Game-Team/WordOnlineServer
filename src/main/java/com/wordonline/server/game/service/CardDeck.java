package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.dto.CardInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

// CardDeck class to manage the deck of cards
@Slf4j
@RequiredArgsConstructor
public class CardDeck {

    private final Random rand = new Random();
    public final List<CardType> cards;

    // random pick card and update to Player Data, Frame Info Dto
    public void drawCard(PlayerData player, CardInfoDto cardInfoDto) {
        if (cards.isEmpty() || player.cards.size() >= PlayerData.MAX_CARD_NUM)
            return;
        int cardIndex = rand.nextInt(cards.size());
        CardType card = cards.get(cardIndex);
        if (player.addCard(card)){ // Added
            cardInfoDto.addCard(card);
            cards.remove(cardIndex);
        }
    }
}
