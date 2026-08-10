package com.wordonline.server.lobby.dto;

/**
 * Body of {@code POST /api/internal/game-sessions/{sessionId}/ended}.
 *
 * <p>The instance id lets the lobby tell "this session really finished here" from a stale
 * report sent by a process that has since restarted.
 */
public record SessionEndedNotificationDto(String instanceId) {
}
