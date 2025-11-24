package com.wordonline.server.deck.dto;

import com.wordonline.server.game.domain.magic.CardType;

public record CardDto(
        long id,
        CardType name,
        CardType.Type type
) {
    public CardDto(CardsDto cardsDto) {
        this(cardsDto.getId(), cardsDto.getName(), cardsDto.getType());
    }

    public CardDto(long id, CardType cardType) {
        this(id, cardType, cardType.getType());
    }
}

