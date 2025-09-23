package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;


public class BurnStatusEffect extends BaseStatusEffect {
    private float damageTick = 0f;
    private static final int DPS = 1;

    public BurnStatusEffect(GameObject owner, float duration) {
        super(owner, duration);
        gameObject.setEffect(Effect.Burn);
    }

    @Override
    public void start() {
        WetStatusEffect wetSE = gameObject.getComponent(WetStatusEffect.class);
        if(wetSE != null)
        {
            wetSE.expire();
            expire();
        }
    }

    @Override
    public void update() {
        float dt = gameObject.getGameLoop().deltaTime;
        damageTick += dt;

        Mob mob = gameObject.getComponent(Mob.class);
        if (damageTick >= 1.0f) {
            if (mob != null) {
                mob.applyDamage(new AttackInfo((DPS * (int)damageTick),ElementType.FIRE));
            }
            damageTick -= (float) Math.floor(damageTick);
        }

        super.update();
    }

    @Override
    public void onAttacked(ElementType attackType) {}

}
