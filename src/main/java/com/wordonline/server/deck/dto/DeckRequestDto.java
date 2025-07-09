package com.wordonline.server.deck.dto;
import java.util.List;

public record DeckRequestDto(
        String name,
        List<Long> cardIds
) {

}