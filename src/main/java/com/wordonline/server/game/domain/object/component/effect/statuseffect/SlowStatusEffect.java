package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class SlowStatusEffect extends BaseStatusEffect {
    private final float amount;
    private float originalSpeed;

    public SlowStatusEffect(GameObject owner, float duration, float amount,StatusEffectKey key) {
        super(owner, duration, key);
        this.amount = amount;
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.getSpeed().setModifierPercent(-amount);
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
        super.expire();
    }
}
