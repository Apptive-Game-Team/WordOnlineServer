package com.wordonline.server.session.dto;

import com.wordonline.server.game.domain.RunType;
import com.wordonline.server.game.domain.SessionType;

import java.util.UUID;

public record SessionDto(
        String sessionId,
        Long uid1,
        Long uid2,
        SessionType sessionType,
        Long scenarioId,
        RunType runType,
        Long parameterProfileId,
        UUID simulationBatchId
) {

}
