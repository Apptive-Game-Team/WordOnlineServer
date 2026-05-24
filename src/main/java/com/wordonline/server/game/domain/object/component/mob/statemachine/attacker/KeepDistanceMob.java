package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.dto.Status;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeepDistanceMob extends Mob {
    private static final float RANGE_TOLERANCE = 0.25f;

    private final Detector detector;
    private final float preferredRange;
    private RigidBody rigidBody;
    private GameObject target;
    private float targetRadius;
    private float detectTimer;

    public KeepDistanceMob(GameObject gameObject, int maxHp, float speed, int targetMask, float preferredRange) {
        super(gameObject, maxHp, speed);
        this.detector = new ClosestEnemyDetector(getGameContext(), targetMask);
        this.preferredRange = preferredRange;
    }

    @Override
    public void start() {
        rigidBody = gameObject.getComponent(RigidBody.class);
        detectTarget();
    }

    @Override
    public void update() {
        super.update();
        detectTimer += getGameContext().getDeltaTime();
        if (detectTimer > Detector.DETECTING_INTERVAL) {
            detectTarget();
            detectTimer = 0f;
        }

        if (!isValidTarget(target)) {
            return;
        }

        double distance = gameObject.getPosition().distance(target.getPosition()) - targetRadius;
        if (distance < preferredRange - RANGE_TOLERANCE) {
            moveAwayFromTarget();
        } else if (distance > preferredRange + RANGE_TOLERANCE) {
            moveTowardTarget();
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    private void detectTarget() {
        target = detector.detect(gameObject);
        targetRadius = target == null
                ? 0f
                : target.getFirstCircleCollider()
                        .map(CircleCollider::getRadius)
                        .orElse(0f);
    }

    private boolean isValidTarget(GameObject target) {
        return target != null
                && target.getStatus() != Status.Destroyed
                && target.getMaster() != gameObject.getMaster();
    }

    private void moveAwayFromTarget() {
        moveInDirection(gameObject.getPosition().toVector2().subtract(target.getPosition().toVector2()));
    }

    private void moveTowardTarget() {
        moveInDirection(target.getPosition().toVector2().subtract(gameObject.getPosition().toVector2()));
    }

    private void moveInDirection(Vector2 direction) {
        if (rigidBody == null) {
            log.warn("[KeepDistanceMoveSkipped] {} has no RigidBody; skipping move update", gameObject.getType());
            return;
        }

        Vector2 normalized = direction.normalize();
        if (normalized.distance(Vector2.ZERO) == 0) {
            return;
        }

        rigidBody.addVelocity(normalized.multiply(speed.total()).toVector3());
    }
}
