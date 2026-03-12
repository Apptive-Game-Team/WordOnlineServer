package com.wordonline.server.game.domain.pve;

import java.util.List;

public record PveScenario(
        String stageId,
        List<PveInstallObject> installers,
        List<PveScenarioEvent> events
) {
}
