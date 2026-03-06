package com.wordonline.server.game.domain.pve;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.List;

public record PveWave(
        int startDelayFrames,
        List<PveSpawn> spawns
) {
    public record PveSpawn(PrefabType prefabType, int count) {}
}
