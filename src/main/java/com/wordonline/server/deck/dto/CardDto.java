package com.wordonline.server.deck.dto;

import com.wordonline.server.game.domain.magic.CardType;

public record CardDto(
        CardType name,
        CardType.Type type
) {
    public CardDto(CardType cardType) {
        this(cardType, cardType.getType());
    }
}
