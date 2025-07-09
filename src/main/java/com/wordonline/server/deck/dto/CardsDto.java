package com.wordonline.server.deck.dto;

import com.wordonline.server.game.domain.magic.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardsDto {
    private final long id;
    private final CardType name;
    private final CardType.Type type;
    public int count;

    public CardsDto(long id, CardType cardType, int count) {
        this(id, cardType, cardType.getType(), count);
    }
}
