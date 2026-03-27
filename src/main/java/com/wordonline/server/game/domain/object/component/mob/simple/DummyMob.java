package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class DummyMob extends Mob {

    public DummyMob(GameObject gameObject, int maxHp) {
        super(gameObject, maxHp, 0);
    }

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {

    }

    @Override
    public void onDestroy() {

    }
}
