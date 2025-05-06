package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.dto.CardInfoDto;
import com.wordonline.server.game.dto.FrameInfoDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardDeck {
    Random rand = new Random();

    List<String> cards = new ArrayList<>(List.of("Dummy", "Dummy", "Dummy", "Dummy", "Dummy", "Dummy"));

    // random pick card and update to Player Data, Frame Info Dto
    public void drawCard(PlayerData player, CardInfoDto cardInfoDto) {
        if (cards.isEmpty())
            return;

        int cardIndex = rand.nextInt(cards.size());
        String card = cards.get(cardIndex);
        if (player.addCard(card)){ // Added
            cardInfoDto.addCard(card);
            cards.remove(cardIndex);
        }
    }
}
