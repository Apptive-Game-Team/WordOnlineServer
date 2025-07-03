package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;

public class ShockStatusEffect extends BaseStatusEffect {
    private static final float STUN_DURATION = 0.5f;
    private int originalSpeed;

    public ShockStatusEffect(GameObject owner) {
        super(owner, STUN_DURATION);
        gameObject.setEffect(Effect.Shock);
    }

    @Override
    public void start() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            originalSpeed = mob.getSpeed();
            mob.setSpeed(0);
        }
    }


    @Override
    public void handleAttack(ElementType attackType) {

    }

    @Override
    protected void expire() {
        Mob mob = gameObject.getComponent(Mob.class);
        if (mob != null) {
            mob.setSpeed(originalSpeed);
        }
        super.expire();
    }
}
