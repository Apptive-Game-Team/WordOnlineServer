package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.CardType;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.dto.CardInfoDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// CardDeck class to manage the deck of cards
public class CardDeck {
    Random rand = new Random();

    // Dummy cards for testing
    // TODO: Replace with actual card data
    List<CardType> cards = new ArrayList<>(List.of(CardType.Dummy, CardType.Dummy, CardType.Dummy, CardType.Dummy, CardType.Dummy, CardType.Dummy));

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
