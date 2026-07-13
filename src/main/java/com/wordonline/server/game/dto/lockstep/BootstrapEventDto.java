package com.wordonline.server.game.dto.lockstep;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

public record BootstrapEventDto(
        int sequence,
        BootstrapEventType type,
        Master master,
        Vector3 position,
        Long scenarioId
) {
}
