package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.DamageInterceptor;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.dto.Effect;

public class BubbleStatusEffect extends BaseStatusEffect implements DamageInterceptor {

    public BubbleStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key, Effect.Bubble);
    }

    @Override
    public boolean beforeDamage(AttackInfo attackInfo) {
        if (attackInfo.getDamage() <= 0) {
            return false;
        }
        expire();
        return true;
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }
}
