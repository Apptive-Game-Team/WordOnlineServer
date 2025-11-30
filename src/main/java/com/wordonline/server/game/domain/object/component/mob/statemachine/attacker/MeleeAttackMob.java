package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;

public class MeleeAttackMob extends AttackMob {

    public MeleeAttackMob(GameObject gameObject, int maxHp, float speed, int targetMask, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval,
                ((CircleCollider) gameObject.getColliders().stream().findFirst().orElse(CircleCollider.dummyCircleCollider(0))).getRadius() + 0.5f);
    }

    @Override
    public void start() {
        super.start();
        setAttackRange(getAttackRange());
    }

    private float getAttackRange() {
        CircleCollider circleCollider = (CircleCollider) gameObject.getColliders().stream().findFirst().orElse(CircleCollider.dummyCircleCollider(0));
        return circleCollider.getRadius() + 0.5f;
    }
}
