package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class PlayerHealthComponent extends Mob {
    private static final int MAX_HEALTH = 100;

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void onDestroy() {}

    @Override
    public void onDeath() {
        gameObject.getGameLoop().resultChecker.setLoser(gameObject.getMaster());
    }

    public PlayerHealthComponent(GameObject gameObject) {
        super(gameObject, MAX_HEALTH, 0, ElementType.NONE);
    }
}
