package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Effect;

public class ShockStatusEffect extends BaseStatusEffect {
    private static final float STUN_DURATION = 1f;

    public ShockStatusEffect(GameObject owner) {
        super(owner, STUN_DURATION);
        gameObject.setEffect(Effect.Shock);
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        ZPhysics zp = gameObject.getComponent(ZPhysics.class);
        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);

        if (mob != null) {
            mob.getSpeed().setModifierPercent(-1f);
        }
        if (zp != null) {
            zp.LockHover(this);
        }
        if(behavior != null) {
            behavior.setStun(1f);
        }
    }


    @Override
    public void onAttacked(ElementType attackType) {

    }

    @Override
    protected void expire() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(0f);
        }
        ZPhysics zp = gameObject.getComponent(ZPhysics.class);
        if (zp != null) {
            zp.UnlockHover(this);
        }
        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);
        if (behavior != null) {
            behavior.setIdle();
        }
        super.expire();
    }
}
