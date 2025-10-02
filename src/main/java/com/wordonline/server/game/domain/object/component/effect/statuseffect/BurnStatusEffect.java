package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Effect;


public class BurnStatusEffect extends BaseStatusEffect {

    public BurnStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key);
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
    public void onAttacked(ElementType attackType) {}

}
