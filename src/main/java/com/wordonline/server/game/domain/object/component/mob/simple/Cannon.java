package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.dto.Status;

public class Cannon extends TimedBehaviorMob {

    private final static float DEFAULT_ATTACK_DURATION = 0.5f;
    private final static float AOE_RADIUS = 1f;

    private float timer = 0;
    private AttackInfo attackInfo;
    private final int targetMask;
    private Detector detector;
    private final float attackDuration;
    private final float attackInterval;
    private final float attackRange;

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask, float attackInterval, float attackRange) {
        this(gameObject, maxHp, damage, targetMask, DEFAULT_ATTACK_DURATION, attackInterval, attackRange);
    }

    public Cannon(GameObject gameObject, int maxHp, int damage, int targetMask, float attackDuration, float attackInterval, float attackRange) {
        super(gameObject, maxHp, 0, DETECT_INTERVAL, null);
        setBehavior(behavior);
        attackInfo = new AttackInfo(damage, ElementType.ROCK);
        this.targetMask = targetMask;
        this.attackDuration = attackDuration;
        this.attackInterval = attackInterval;
        this.attackRange = attackRange;
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
        List<GameObject> victims = getGameContext().overlapSphereAll(target,AOE_RADIUS);
        for (GameObject victim : victims) {
            Mob mob = victim.getComponent(Mob.class);
            if (mob == null) {
                continue;
            }

            mob.onDamaged(attackInfo, attackDuration);
        }        
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
