package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class MeleeAttacker extends Attacker {

    public MeleeAttacker(GameObject gameObject, int maxHp, float speed, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, damage, attackInterval, 1);
    }
}
