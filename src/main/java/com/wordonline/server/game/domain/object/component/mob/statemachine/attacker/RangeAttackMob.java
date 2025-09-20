package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class RangeAttackMob extends AttackMob {

    public RangeAttackMob(GameObject gameObject, int maxHp, float speed, int damage, float attackInterval, float attackRange) {
        super(gameObject, maxHp, speed, damage, attackInterval, attackRange);
    }
}
