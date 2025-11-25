package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.dto.Status;

public class Cannon extends TimedBehaviorMob {

    private final static float DETECT_INTERVAL = 5;
    private final static float ATTACK_RANGE = 5;
    private final static float DEFAULT_ATTACK_DURATION = 0.5f;

    private float timer = 0;
    private AttackInfo attackInfo;
    private final int targetMask;
    private Detector detector;
    private float attackDuration;

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask) {
        this(gameObject, maxHp, damage, targetMask, DEFAULT_ATTACK_DURATION);
    }

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask, float attackDuration) {
        super(gameObject, maxHp, 0, DETECT_INTERVAL, null);
        setBehavior(behavior);
        attackInfo = new AttackInfo(damage, ElementType.ROCK);
        this.targetMask = targetMask;
        this.attackDuration = attackDuration;
    }

    private final Behavior behavior = () -> {
        GameObject target = detector.detect(gameObject);

        if (target == null) {
            return false;
        }

        double distance = target.getPosition().distance(gameObject.getPosition());

        if (distance > ATTACK_RANGE) {
            return false;
        }

        getGameContext().getObjectsInfoDtoBuilder()
                .createProjection(gameObject, target, "RockShot", attackDuration);
        target.getComponent(Mob.class)
                .onDamaged(attackInfo, attackDuration);
        gameObject.setStatus(Status.Attack);
        return true;
    };


    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {
        this.detector = new ClosestEnemyDetector(getGameContext(), targetMask);
    }

    @Override
    public void onDestroy() {

    }
}
