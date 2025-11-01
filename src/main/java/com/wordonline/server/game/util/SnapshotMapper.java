package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.dto.frame.SnapshotObjectDto;

public final class SnapshotMapper {

    public static SnapshotObjectDto toDto(GameObject g) {
        var p = g.getPosition();
        Mob mob = g.getComponent(Mob.class);

        return new SnapshotObjectDto(
                g.getId(),
                g.getType().name(),
                p.getX(), p.getY(), p.getZ(),
                g.getMaster().toString(),
                g.getStatus() == null ? Status.Idle.name() : g.getStatus().name(),
                g.getEffect() == null ? Effect.None.name() : g.getEffect().name(),
                mob != null ? mob.getHp() : 0,
                mob != null ? mob.getMaxHp() : 0
        );
    }
}