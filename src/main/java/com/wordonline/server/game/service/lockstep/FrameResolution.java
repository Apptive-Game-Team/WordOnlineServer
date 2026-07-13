package com.wordonline.server.game.service.lockstep;

import com.wordonline.server.game.dto.lockstep.ConfirmedInputDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record FrameResolution(
        int frameNum,
        List<ConfirmedInputDto> inputs,
        Map<Long, String> hashes,
        Set<Long> missingParticipantIds,
        boolean hashMatched
) {
    public boolean complete() {
        return missingParticipantIds.isEmpty();
    }
}
