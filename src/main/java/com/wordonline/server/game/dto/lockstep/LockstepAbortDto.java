package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record LockstepAbortDto(
        int frameNum,
        LockstepAbortReason reason,
        Set<Long> participantIds
) {
    public LockstepAbortDto {
        participantIds = Set.copyOf(participantIds);
    }

    @JsonProperty("type")
    public LockstepMessageType type() {
        return LockstepMessageType.ABORT;
    }
}
