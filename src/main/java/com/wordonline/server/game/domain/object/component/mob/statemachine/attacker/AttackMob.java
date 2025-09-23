package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Status;

public class AttackMob extends BehaviorMob {

    public AttackMob(GameObject gameObject, int maxHp,
            float speed, int targetMask, int damage, float attackInterval, float attackRange) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, (target) -> {
            Damageable attackable = target.getComponent(Damageable.class);

            if (attackable == null) {
                return false;
            }

            attackable.onDamaged(new AttackInfo(damage, gameObject.getElement().total()));
            gameObject.setStatus(Status.Attack);
            return true;
        });
    }
}
