package com.wordonline.server.game.dto.lockstep;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;

import java.util.List;

public record FrameInputDto(
        int sequence,
        LockstepInputType type,
        int id,
        List<CardType> cards,
        Vector3 position
) {
    public FrameInputDto {
        cards = cards == null ? List.of() : List.copyOf(cards);
    }
}
