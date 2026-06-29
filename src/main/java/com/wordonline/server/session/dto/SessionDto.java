package com.wordonline.server.session.dto;

import com.wordonline.server.game.domain.SessionType;

public record SessionDto(
        String sessionId,
        Long uid1,
        Long uid2,
        SessionType sessionType,
        Long scenarioId
) {

}
