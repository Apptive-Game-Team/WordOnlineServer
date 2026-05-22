package com.wordonline.server.game.domain.object.component.mob.simple;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class Tower extends TimedBehaviorMob {

    private static final float DEFAULT_ATTACK_DURATION = 0.2f;
    private static final float SPLASH_RADIUS = 1f;

    private final AttackInfo attackInfo;
    private final int targetMask;
    private final float attackDuration;
    private final float attackRange;
    private Detector detector;

    public Tower(GameObject gameObject, int maxHp, int damage, int targetMask, float attackInterval, float attackRange) {
        this(gameObject, maxHp, damage, targetMask, DEFAULT_ATTACK_DURATION, attackInterval, attackRange);
    }

    public Tower(GameObject gameObject, int maxHp, int damage, int targetMask, float attackDuration, float attackInterval, float attackRange) {
        super(gameObject, maxHp, 0, attackInterval, null);
        setBehavior(this::attack);
        this.attackInfo = new AttackInfo(damage, ElementType.ROCK);
        this.targetMask = targetMask;
        this.attackDuration = attackDuration;
        this.attackRange = attackRange;
    }

    private boolean attack() {
        GameObject target = detector.detect(gameObject);

        if (target == null) {
            return false;
        }

        double distance = gameObject.getPosition().toVector2().distance(target.getPosition().toVector2());
        if (distance > attackRange) {
            return false;
        }

        Damageable damageable = target.getComponent(Damageable.class);
        if (damageable == null) {
            return false;
        }

        applySplashDamage(target);
        getGameContext().getObjectsInfoDtoBuilder()
                .createProjection(gameObject, target, "RockShot", attackDuration);
        gameObject.setStatus(Status.Attack);
        return true;
    }

    private void applySplashDamage(GameObject centerTarget) {
        List<GameObject> victims = getGameContext().overlapSphereAll(centerTarget, SPLASH_RADIUS);
        for (GameObject candidate : victims) {
            if (candidate.getMaster() == gameObject.getMaster()) {
                continue;
            }

            Damageable damageable = candidate.getComponent(Damageable.class);
            if (damageable == null) {
                continue;
            }

            damageable.onDamaged(attackInfo, attackDuration);
        }
    }

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {
        detector = new ClosestEnemyDetector(getGameContext(), targetMask, true);
        gameObject.drawCircle(Vector3.ZERO, attackRange, GizmoCategory.AttackRange);
    }

    @Override
    public void onDestroy() {
    }
}
