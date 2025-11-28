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

        // 공격 로직: 사거리 안 적 최대 2명 찾고 미사일 2발 분배
        setBehavior(target -> {
            // BehaviorMob.attackRange는 같은 패키지라 접근 가능
            List<GameObject> targets = multiEnemyDetector.detectInRange(gameObject, 2, attackRange);

            if (targets.isEmpty()) {
                // 때릴 놈 없으면 false -> AttackState에서 Idle로 전환
                return false;
            }

            if (targets.size() == 1) {
                GameObject only = targets.get(0);
                shoot(only);
                shoot(only); // 한 놈에게 2발
            } else {
                shoot(targets.get(0));
                shoot(targets.get(1)); // 두 놈에게 각각 1발
            }

            return true;
        });
    }

    private void shoot(GameObject target) {
        gameObject.getGameContext()
                .getObjectsInfoDtoBuilder()
                .createProjection(gameObject, target, projectileType, projectileDuration);

        target.getComponentOptional(Damageable.class)
                .ifPresent(damageable -> {
                    damageable.onDamaged(new AttackInfo(damage, gameObject.getElement().total()));
                    gameObject.setStatus(Status.Attack);
                });
    }
}
