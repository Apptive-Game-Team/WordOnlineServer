package com.wordonline.server.game.dto;

import java.util.List;

import com.wordonline.server.game.domain.magic.CardType;

public record MagicInfoDto(
        Long id,
        String name,
        List<CardType> cards
) {

}