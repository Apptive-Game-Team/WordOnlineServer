package com.wordonline.server.game.dto.lockstep;

public record ClientReadyDto(
        int protocolVersion,
        String simulationVersion,
        String configVersion
) {
}
