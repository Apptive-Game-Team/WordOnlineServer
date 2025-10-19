package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.object.GameObject;

public class ManaWellMob extends Mob {

    public ManaWellMob(GameObject gameObject, int maxHp) {
        super(gameObject, maxHp, 0);
    }

    @Override
    public void onDeath() {
        getGameContext().getGameSessionData()
                .getPlayerData(gameObject.getMaster())
                .manaCharger
                .updateManaCharge(-1);
        gameObject.destroy();
    }

    @Override
    public void start() {
        getGameContext().getGameSessionData()
                .getPlayerData(gameObject.getMaster())
                .manaCharger
                .updateManaCharge(1);
    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }
}
