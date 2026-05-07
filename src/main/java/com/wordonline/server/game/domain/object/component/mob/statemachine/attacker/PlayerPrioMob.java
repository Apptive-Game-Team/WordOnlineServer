package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.detector.PlayerPriorityEnemyDetector;

public class PlayerPrioMob extends MeleeAttackMob {
    private final int targetMask;

    public PlayerPrioMob(GameObject gameObject, int maxHp, float speed, int targetMask, int damage, float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval);
        this.targetMask = targetMask;
    }

    @Override
    public void start() {
        super.start();
        detector = new PlayerPriorityEnemyDetector(getGameContext(), targetMask);
    }
}
