package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Status;

public class RockGolemMob extends MeleeAttackMob {

    private static final float KNOCKBACK_STRENGTH = 0.25f;

    public RockGolemMob(GameObject gameObject,
                        int maxHp,
                        float speed,
                        int targetMask,
                        int damage,
                        float attackInterval) {
        super(gameObject, maxHp, speed, targetMask, damage, attackInterval);

        setBehavior(target -> {
            Damageable damageable = target.getComponent(Damageable.class);
            if (damageable == null) {
                return false;
            }

            damageable.onDamaged(new AttackInfo(damage, gameObject.getElement().total()));

            EffectReceiver effectReceiver = target.getComponent(EffectReceiver.class);
            if (effectReceiver != null) {
                Vector3 direction = target.getPosition()
                        .subtract(gameObject.getPosition())
                        .grounded()
                        .normalize();
                effectReceiver.onReceive(Effect.Knockback, direction, KNOCKBACK_STRENGTH);
            }

            gameObject.setStatus(Status.Attack);
            return true;
        });
    }
}
