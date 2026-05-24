package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;

public class NonAttackingCowardMob extends CowardMob {
    private final float preferredRange;

    public NonAttackingCowardMob(GameObject gameObject,
                                 int maxHp,
                                 float speed,
                                 int targetMask,
                                 float attackInterval,
                                 float detectionRange,
                                 float panicDuration,
                                 float preferredRange) {
        super(gameObject, maxHp, speed, targetMask, 0, attackInterval, detectionRange, panicDuration);
        this.preferredRange = preferredRange;
        setBehavior(target -> true);
    }

    @Override
    public void start() {
        super.start();
        attackRange = preferredRange;
    }
}
