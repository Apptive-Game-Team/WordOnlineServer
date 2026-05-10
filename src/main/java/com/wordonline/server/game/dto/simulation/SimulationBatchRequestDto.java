package com.wordonline.server.game.dto.simulation;

public record SimulationBatchRequestDto(
        long leftUserId,
        long rightUserId,
        Integer matchCount,
        Long parameterProfileId
) {
}
