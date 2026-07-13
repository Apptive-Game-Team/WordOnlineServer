package com.wordonline.server.game.dto.lockstep;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ConfirmedFrameDto(
        int protocolVersion,
        int frameNum,
        List<ConfirmedInputDto> inputs,
        Map<Long, String> previousFrameHashes,
        boolean hashMatched
) {
    public ConfirmedFrameDto {
        inputs = List.copyOf(inputs);
        previousFrameHashes = Collections.unmodifiableMap(new LinkedHashMap<>(previousFrameHashes));
    }

    @JsonProperty("type")
    public LockstepMessageType type() {
        return LockstepMessageType.CONFIRMED_FRAME;
    }
}
