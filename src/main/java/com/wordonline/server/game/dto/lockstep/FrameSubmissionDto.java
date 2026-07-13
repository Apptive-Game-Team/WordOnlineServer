package com.wordonline.server.game.dto.lockstep;

import java.util.List;

public record FrameSubmissionDto(
        int protocolVersion,
        int frameNum,
        String previousFrameHash,
        List<FrameInputDto> inputs
) {
    public FrameSubmissionDto {
        previousFrameHash = previousFrameHash == null ? "" : previousFrameHash;
        inputs = inputs == null ? List.of() : List.copyOf(inputs);
    }
}
