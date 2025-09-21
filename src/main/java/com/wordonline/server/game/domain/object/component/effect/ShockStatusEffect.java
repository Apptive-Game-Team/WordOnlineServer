package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.dto.Effect;

public class ShockStatusEffect extends BaseStatusEffect {
    private static final float STUN_DURATION = 0.5f;

    public ShockStatusEffect(GameObject owner) {
        super(owner, STUN_DURATION);
        gameObject.setEffect(Effect.Shock);
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(-1f);
        RigidBody rb = gameObject.getComponent(RigidBody.class);
        if(rb != null) {
            rb.addNormalVelocity(-10f);
        }
        }
    }


    @Override
    public void handleAttack(ElementType attackType) {

    }

    @Override
    protected void expire() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(0f);
        }
        super.expire();
    }
}
