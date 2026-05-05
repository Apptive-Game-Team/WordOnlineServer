package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.simple.PlayerHealthComponent;

public enum TargetCategory {
    UNIT,
    BUILDING,
    PLAYER,
    UNKNOWN;

    public static TargetCategory of(GameObject target) {
        if (target.hasComponent(PlayerHealthComponent.class)) {
            return PLAYER;
        }

        Mob mob = target.getComponent(Mob.class);
        if (mob != null) {
            return mob.getSpeed().total() > 0f ? UNIT : BUILDING;
        }

        if (target.hasComponent(Damageable.class)) {
            return BUILDING;
        }

        return UNKNOWN;
    }
}
