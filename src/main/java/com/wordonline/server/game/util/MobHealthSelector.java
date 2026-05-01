package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

import java.util.List;

public final class MobHealthSelector {

    private MobHealthSelector() {
    }

    public static Mob findHealthMob(GameObject gameObject) {
        if (gameObject == null) {
            return null;
        }

        List<Mob> mobs = gameObject.getComponents(Mob.class);
        if (mobs.isEmpty()) {
            return null;
        }

        return mobs.stream()
                .filter(mob -> mob.getMaxHp() > 0)
                .findFirst()
                .orElse(mobs.get(0));
    }
}
