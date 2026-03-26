package com.wordonline.server.debug.dto;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

public record DebugSummonMagicRequestDto(
        String sessionId,
        Master master,
        Long magicId,
        Vector3 position
) {
}
