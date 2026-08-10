package com.wordonline.server.session.dto;

/**
 * @param instanceId boot generation of the process that owns the created session. The lobby
 *                   stores it on the ticket and later compares it with the id published on
 *                   the {@code servers} row; a mismatch means this process restarted and the
 *                   session no longer exists.
 */
public record SessionReadyResponse(
        String attemptId,
        String sessionId,
        boolean ready,
        String serverUrl,
        String webSocketUrl,
        String instanceId
) {
}
