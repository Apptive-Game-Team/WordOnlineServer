package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class ManaWellMob extends Mob {

    private static final float DELTA_VALUE = 0.8f;
    private static final int SELF_DAMAGE_PER_SEC = 1;

    public ManaWellMob(GameObject gameObject, int maxHp) {
        super(gameObject, maxHp, 0);
    }

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {
        getGameContext().getGameSessionData()
                .getPlayerData(gameObject.getMaster())
                .manaCharger
                .updateManaCharge(DELTA_VALUE);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void onDestroy() {
        getGameContext().getGameSessionData()
                .getPlayerData(gameObject.getMaster())
                .manaCharger
                .updateManaCharge(-DELTA_VALUE);
    }
}
