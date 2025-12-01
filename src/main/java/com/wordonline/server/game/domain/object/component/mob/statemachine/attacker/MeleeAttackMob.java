package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import java.util.Optional;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.Collider;

public class MeleeAttackMob extends AttackMob {

    private static final float DEFAULT_ATTACK_RANGE = 0.5f;

    public MeleeAttackMob(GameObject gameObject, int maxHp, float speed, int targetMask, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval, DEFAULT_ATTACK_RANGE);
    }

    @Override
    public void start() {
        super.start();
        attackRange = getAttackRange();
    }

    private float getAttackRange() {
        Optional<CircleCollider> circleCollider = gameObject.getColliders()
                .stream()
                .filter(CircleCollider.class::isInstance)
                .filter(Collider::isNotTrigger)
                .findFirst()
                .map(CircleCollider.class::cast);
        return circleCollider.map(collider -> collider.getRadius() + DEFAULT_ATTACK_RANGE)
                .orElse(DEFAULT_ATTACK_RANGE);
    }
}
