package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class NonAttackingCowardMob extends CowardMob {

    public NonAttackingCowardMob(GameObject gameObject,
                                 int maxHp,
                                 float speed,
                                 int targetMask,
                                 float attackInterval,
                                 float detectionRange,
                                 float panicDuration) {
        super(gameObject, maxHp, speed, targetMask, 0, attackInterval, detectionRange, panicDuration);
        setBehavior(target -> true);
    }
}
