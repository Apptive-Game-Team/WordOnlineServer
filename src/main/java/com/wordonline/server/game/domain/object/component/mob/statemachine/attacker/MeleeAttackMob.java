package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class MeleeAttackMob extends AttackMob {

    public MeleeAttackMob(GameObject gameObject, int maxHp, float speed, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, damage, attackInterval, 1);
    }
}
