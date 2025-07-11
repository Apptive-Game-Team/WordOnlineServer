package com.wordonline.server.deck.dto;

import com.wordonline.server.game.domain.magic.CardType;

public record DeckCardDto(
        long deckId,
        long cardId,
        int count,
        String deckName,
        CardType cardName,
        CardType.Type type
) {
}
