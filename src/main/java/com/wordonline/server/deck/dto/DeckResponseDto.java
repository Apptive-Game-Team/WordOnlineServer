package com.wordonline.server.deck.dto;

import java.util.List;

public record DeckResponseDto(
        long id,
        String name,
        List<CardDto> cards
) {

}
