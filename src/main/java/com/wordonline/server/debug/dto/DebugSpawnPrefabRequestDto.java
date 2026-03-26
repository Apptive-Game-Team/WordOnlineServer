package com.wordonline.server.debug.dto;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

public record DebugSpawnPrefabRequestDto(
        String sessionId,
        Master master,
        String prefabId,
        PrefabType prefabType,
        Vector3 position
) {
}
