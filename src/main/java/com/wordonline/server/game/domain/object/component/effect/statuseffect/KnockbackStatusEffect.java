package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Effect;

public class KnockbackStatusEffect extends BaseStatusEffect {
    private static final float KNOCKBACK_DURATION = 0.5f;
    private static final float KNOCKBACK_POWER    = 2f;
    private static final float KNOCKBACK_POWER_Z  = 10f;
    private static final float PROX_MIN           = 0.2f;
    private final Vector2 knockbackDir;
    private final float proximity;

    // 누적 이동 거리
    private float moved = 0f;

    public KnockbackStatusEffect(GameObject owner, Vector2 dir, float prox, StatusEffectKey key) {
        super(owner, KNOCKBACK_DURATION, key);
        gameObject.setEffect(Effect.Knockback);
        this.knockbackDir = dir;
        this.proximity = Math.max(prox, PROX_MIN);
    }

    @Override
    public void start() {
        ZPhysics zP = gameObject.getComponent(ZPhysics.class);
        if(zP != null) zP.addImpulseZ(KNOCKBACK_POWER_Z * proximity);

        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);
        if (behavior != null) behavior.setStun(KNOCKBACK_DURATION);
    }

    @Override
    public void update() {
        float dt = gameObject.getGameLoop().deltaTime;
        float speed = KNOCKBACK_POWER / KNOCKBACK_DURATION;
        float step  = speed * dt;

        float remain = KNOCKBACK_POWER - moved;
        if (step > remain) step = remain;
        if (step <= 0f) {
            expire();
            return;
        }

        RigidBody rb = gameObject.getComponent(RigidBody.class);
        if(rb != null)
        {
            rb.addVelocity(knockbackDir.multiply(step).multiply(proximity));
        }
        moved += step;
    }

    @Override
    public void onAttacked(ElementType attackType) { }

    @Override
    protected void expire() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(0f);
            ZPhysics zP = gameObject.getComponent(ZPhysics.class);
            if(zP != null) {
                zP.addImpulseZ(-KNOCKBACK_POWER_Z * proximity);
            }
        }
        super.expire();
    }
}
