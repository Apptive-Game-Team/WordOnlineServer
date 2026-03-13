package com.wordonline.server.game.domain.pve;

import java.util.List;

public record PveScenarioEvent(
        String id,
        PveTriggerType type,
        int value,
        String speakerInstallerId,
        String key,
        List<String> lines
) {
}
