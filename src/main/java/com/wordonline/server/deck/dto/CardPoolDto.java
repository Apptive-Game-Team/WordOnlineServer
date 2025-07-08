package com.wordonline.server.deck.dto;

import java.util.List;

public record CardPoolDto(
        List<CardDto> cards
) {
}
