package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BehaviorMob;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Effect;

public class ShockStatusEffect extends BaseStatusEffect {
    private final float duration;

    public ShockStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key);
        this.duration = duration;
        gameObject.setEffect(Effect.Shock);
    }

    @Override
    public void start() {
        ZPhysics zp = gameObject.getComponent(ZPhysics.class);
        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);

        if (zp != null) {
            zp.lockHover(this);
        }
        if(behavior != null) {
            behavior.setStun(duration);
        }
    }


    @Override
    public void onAttacked(ElementType attackType) {

    }

    @Override
    protected void expire() {
        ZPhysics zp = gameObject.getComponent(ZPhysics.class);
        if (zp != null) {
            zp.unlockHover(this);
        }
        BehaviorMob behavior = gameObject.getComponent(BehaviorMob.class);
        if (behavior != null) {
            behavior.setIdle();
        }
        super.expire();
    }
}
