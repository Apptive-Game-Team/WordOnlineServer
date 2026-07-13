package com.wordonline.server.game.dto.lockstep;

import java.util.Set;

public record LockstepAbortDto(
        String type,
        int frameNum,
        String reason,
        Set<Long> participantIds
) {
    public LockstepAbortDto {
        participantIds = Set.copyOf(participantIds);
    }
}
