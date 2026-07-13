package com.wordonline.server.game.dto.lockstep;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ConfirmedFrameDto(
        String type,
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
}
