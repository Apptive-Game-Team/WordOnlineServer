package com.wordonline.server.session.service;

public record SessionCreationResult(
        String attemptId,
        String sessionId,
        boolean ready
) {
}
