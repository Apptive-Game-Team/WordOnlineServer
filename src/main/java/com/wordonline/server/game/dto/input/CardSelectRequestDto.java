package com.wordonline.server.game.dto.input;

import com.wordonline.server.game.domain.magic.CardType;

public record CardSelectRequestDto(
        String type,
        CardType card,
        int id
) {
}
