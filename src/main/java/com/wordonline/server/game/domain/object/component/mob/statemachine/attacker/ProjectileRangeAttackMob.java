package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Status;

public class ProjectileRangeAttackMob extends BehaviorMob {

    public ProjectileRangeAttackMob(GameObject gameObject,
            int maxHp, float speed, int targetMask, int damage, float attackInterval, float attackRange, String projectileType, float projectileDuration) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, (target) -> {
            gameObject.getGameContext()
                    .getObjectsInfoDtoBuilder()
                    .createProjection(gameObject, target, projectileType, projectileDuration);

            target.getComponentOptional(Damageable.class)
                    .ifPresent(damageable -> {
                        damageable.onDamaged(new AttackInfo(damage, gameObject.getElement().total()));
                        gameObject.setStatus(Status.Attack);
                    });

            return true;
        });
    }
}
