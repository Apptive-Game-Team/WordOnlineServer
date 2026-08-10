package com.wordonline.server.session.dto;

import com.wordonline.server.game.domain.SessionType;

public record CreateSessionRequest(
        String attemptId,
        String sessionId,
        Long uid1,
        Long uid2,
        SessionType sessionType,
        Long scenarioId
) {
    public SessionDto toSessionDto() {
        return new SessionDto(sessionId, uid1, uid2, sessionType, scenarioId);
    }
}
