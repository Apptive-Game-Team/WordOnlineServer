package com.wordonline.server.session.dto;

public record SessionDto(
        String sessionId,
        Long uid1,
        Long uid2
) {

}
