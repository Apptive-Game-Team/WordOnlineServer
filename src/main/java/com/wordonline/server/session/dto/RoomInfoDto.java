package com.wordonline.server.session.dto;

public record RoomInfoDto(
        String sessionId,
        Long leftUserId,
        Long rightUserId,
        String serverUrl
) {

}
