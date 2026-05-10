package com.wordonline.server.game.dto.simulation;

import java.util.UUID;

public record SimulationBatchResponseDto(
        UUID simulationBatchId,
        long leftUserId,
        long rightUserId,
        int requestedMatchCount,
        int completedMatchCount,
        int leftWins,
        int rightWins,
        int draws,
        Long parameterProfileId
) {
}
