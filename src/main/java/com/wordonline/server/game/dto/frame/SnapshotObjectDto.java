package com.wordonline.server.game.dto.frame;

import java.util.List;

import com.wordonline.server.game.domain.debug.Gizmo;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Status;

public record SnapshotObjectDto(
        int id,
        String prefab,
        float x, float y, float z,
        String master,
        Status status,
        Effect effect,
        int hp,
        int maxHp,
        List<Gizmo> gizmos
) {}