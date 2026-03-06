package com.wordonline.server.game.domain.pve;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.List;

public record PveScenario(
        String stageId,
        List<PveWave> waves,
        List<PveTrigger> triggers,
        List<PrefabType> enemyPrefabTypes
) {
}
