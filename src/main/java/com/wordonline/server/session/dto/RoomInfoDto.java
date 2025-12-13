package com.wordonline.server.session.dto;

public record RoomInfoDto(
        String sessionId,
        Long uid1,
        Long uid2,
        String serverUrl
) {

}
