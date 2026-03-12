package com.wordonline.server.game.domain.pve;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

import java.util.List;

public record PveInstallObject(
        String installerId,
        PrefabType prefabType,
        Master master,
        Vector3 position,
        List<List<CardType>> magicRecipes,
        float castIntervalSec
) {
}
