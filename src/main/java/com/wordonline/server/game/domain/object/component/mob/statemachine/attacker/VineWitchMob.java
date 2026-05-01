package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineFanMagic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class VineWitchMob extends PVEBossMob {

    private static final int RAGE_WAVE_COUNT = 4;
    private static final float RAGE_CAST_INTERVAL_SEC = 0.45f;

    private final VineFanMagic vineFanMagic;
    private boolean ragePatternActivated = false;
    private int remainingRageCastCount = 0;
    private float rageCastTimer = 0f;

    public VineWitchMob(GameObject gameObject,
                        int maxHp,
                        float speed,
                        int targetMask,
                        float attackInterval,
                        float attackRange,
                        List<Magic> magics,
                        VineFanMagic vineFanMagic) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, magics);
        this.vineFanMagic = vineFanMagic;
    }

    @Override
    public void onDamaged(AttackInfo attackInfo) {
        super.onDamaged(attackInfo);

        if (!ragePatternActivated && hp > 0 && hp <= maxHp / 2) {
            ragePatternActivated = true;
            remainingRageCastCount = RAGE_WAVE_COUNT;
            // Trigger first cast immediately on next update tick.
            rageCastTimer = RAGE_CAST_INTERVAL_SEC;
        }
    }

    @Override
    public void update() {
        super.update();
        handleRagePatternCasts();
    }

    @Override
    protected boolean castMagic(GameObject target) {
        if (remainingRageCastCount > 0) {
            if (vineFanMagic == null) {
                remainingRageCastCount = 0;
                return super.castMagic(target);
            }
            return true;
        }
        return super.castMagic(target);
    }

    private void handleRagePatternCasts() {
        if (remainingRageCastCount <= 0 || vineFanMagic == null) {
            return;
        }

        GameObject rageTarget = resolveRageTarget();
        if (!isRageTargetInRange(rageTarget)) {
            return;
        }

        rageCastTimer += getGameContext().getDeltaTime();
        while (rageCastTimer >= RAGE_CAST_INTERVAL_SEC && remainingRageCastCount > 0) {
            rageCastTimer -= RAGE_CAST_INTERVAL_SEC;
            castRageFan(rageTarget);
            remainingRageCastCount--;
        }
    }

    private void castRageFan(GameObject rageTarget) {
        Vector3 castOrigin = gameObject.getPosition();
        vineFanMagic.run(getGameContext(), gameObject.getMaster(), castOrigin, rageTarget.getPosition());
    }

    private GameObject resolveRageTarget() {
        if (target != null && target.getStatus() != Status.Destroyed) {
            return target;
        }

        GameObject detected = detector.detect(gameObject);
        if (detected == null || detected.getStatus() == Status.Destroyed) {
            return null;
        }

        target = detected;
        if (!detected.getColliders().isEmpty() && detected.getColliders().getFirst() instanceof CircleCollider circleCollider) {
            targetRadius = circleCollider.getRadius();
        } else {
            targetRadius = 0f;
        }
        return target;
    }

    private boolean isRageTargetInRange(GameObject rageTarget) {
        if (rageTarget == null || rageTarget.getStatus() == Status.Destroyed) {
            return false;
        }

        double distanceToTarget = gameObject.getPosition().distance(rageTarget.getPosition()) - targetRadius;
        return distanceToTarget <= attackRange;
    }
}
