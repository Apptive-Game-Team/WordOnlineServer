package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class MeleeAttackMob extends AttackMob {

    private static final float DEFAULT_ATTACK_RANGE = 0.5f;

    public MeleeAttackMob(GameObject gameObject, int maxHp, float speed, int targetMask, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval, DEFAULT_ATTACK_RANGE);
    }
}
