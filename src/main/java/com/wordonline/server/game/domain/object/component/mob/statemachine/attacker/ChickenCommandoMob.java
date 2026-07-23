package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class ChickenCommandoMob extends MeleeAttackMob {

    private static final float GROUNDED_THRESHOLD = 0.05f;

    public ChickenCommandoMob(GameObject gameObject,
                              int maxHp,
                              float speed,
                              int targetMask,
                              int damage,
                              float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval);
    }

    @Override
    public void update() {
        if (gameObject.getPosition().getY() > GROUNDED_THRESHOLD) {
            return;
        }
        super.update();
    }
}
