package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.magic.CardType;

import java.util.List;

public record LockstepSessionStartDto(
        int protocolVersion,
        String simulationVersion,
        String configVersion,
        long rngSeed,
        int initialFrame,
        SessionType sessionType,
        long leftUserId,
        long rightUserId,
        List<CardType> leftCards,
        List<CardType> rightCards
) {
    public LockstepSessionStartDto {
        leftCards = List.copyOf(leftCards);
        rightCards = List.copyOf(rightCards);
    }

    @JsonProperty("type")
    public LockstepMessageType type() {
        return LockstepMessageType.SESSION_START;
    }
}
