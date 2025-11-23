package com.wordonline.server.debug.dto;

import com.wordonline.server.game.dto.Master;

public record DebugGameRequestDto(
        Master side,
        long userId
) {

}
