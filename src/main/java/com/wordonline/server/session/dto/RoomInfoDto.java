package com.wordonline.server.session.dto;

import java.time.Instant;

public record RoomInfoDto(
        String sessionId,
        Long leftUserId,
        Long rightUserId,
        String serverUrl,
        Instant createdAt
) {

}
