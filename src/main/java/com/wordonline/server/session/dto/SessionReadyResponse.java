package com.wordonline.server.session.dto;

public record SessionReadyResponse(
        String attemptId,
        String sessionId,
        boolean ready,
        String serverUrl,
        String webSocketUrl
) {
}
