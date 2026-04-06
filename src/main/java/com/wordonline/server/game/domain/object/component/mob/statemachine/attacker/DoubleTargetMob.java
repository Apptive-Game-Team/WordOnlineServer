package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.mob.detector.MultiEnemyDetector;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class DoubleTargetMob extends BehaviorMob {

    private final MultiEnemyDetector multiEnemyDetector;

    private final int damage;
    private final String projectileType;
    private final float projectileDuration;

    public DoubleTargetMob(GameObject gameObject,
                           int maxHp,
                           float speed,
                           int targetMask,
                           int damage,
                           float attackInterval,
                           float attackRange,
                           String projectileType,
                           float projectileDuration) {

        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, null);

        this.damage = damage;
        this.projectileType = projectileType;
        this.projectileDuration = projectileDuration;

        this.multiEnemyDetector = new MultiEnemyDetector(getGameContext(), targetMask);
        this.detector = multiEnemyDetector;

        setBehavior(target -> {
            if (target == null) return false;

            Damageable d = target.getComponent(Damageable.class);
            if (d == null) return false;

            shoot(target);

            List<GameObject> targets = multiEnemyDetector.detectInRange(gameObject, 2, attackRange);

            GameObject second = null;
            for (GameObject t : targets) {
                if (t != target) {
                    second = t;
                    break;
                }
            }

            if (second == null) {
                shoot(target);
            } else {
                shoot(second);
            }

            return true;
        });
    }

    private void shoot(GameObject target) {

        target.getComponentOptional(Damageable.class)
                .ifPresent(damageable -> {
                    damageable.onDamaged(new AttackInfo(damage, gameObject.getElement().total()));
                    gameObject.setStatus(Status.Attack);
                });
    }
}
