package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetRelation;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class BubbleSpiritMob extends BehaviorMob {

    private static final String PROJECTILE_TYPE = "BubbleShot";
    private static final float SPLASH_RADIUS = 1f;

    private final int damage;
    private final float projectileSpeed;

    public BubbleSpiritMob(GameObject gameObject,
            int maxHp, float speed, int damage, float attackInterval, float attackRange, float projectileSpeed) {
        super(gameObject, maxHp, speed, TargetMask.AIR.bit, attackInterval, attackRange, null);
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
        setBehavior(this::shootBubble);
    }

    private boolean shootBubble(GameObject target) {
        float projectileDuration = (float) (target.getPosition().distance(gameObject.getPosition()) / projectileSpeed);
        getGameContext().getObjectsInfoDtoBuilder()
                .createProjection(gameObject, target, PROJECTILE_TYPE, projectileDuration);

        applySplashDamage(target, projectileDuration);
        gameObject.setStatus(Status.Attack);
        return true;
    }

    private void applySplashDamage(GameObject centerTarget, float delay) {
        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total());
        List<GameObject> victims = getGameContext().overlapSphereAll(centerTarget, SPLASH_RADIUS);

        for (GameObject candidate : victims) {
            if (!TargetRelation.canAttack(gameObject, candidate)) {
                continue;
            }
            if ((TargetMask.of(candidate) & TargetMask.AIR.bit) == 0) {
                continue;
            }

            Damageable damageable = candidate.getComponent(Damageable.class);
            if (damageable == null) {
                continue;
            }

            damageable.onDamaged(attackInfo, delay);
        }
    }
}
