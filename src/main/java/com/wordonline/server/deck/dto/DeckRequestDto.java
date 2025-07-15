package com.wordonline.server.deck.dto;

import com.wordonline.server.deck.validation.DeckValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DeckRequestDto(
        @NotBlank(message = "Deck name must not be blank")
        @Size(max = 31, message = "Deck name must be at most 31 characters")
        String name,
        @Size(max = 10, min = 10, message = "Num of cards must be 10")
        @DeckValid
        List<Long> cardIds
) {

}