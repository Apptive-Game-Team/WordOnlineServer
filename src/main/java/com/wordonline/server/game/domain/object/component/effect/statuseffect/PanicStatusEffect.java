package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.dto.Effect;

public class PanicStatusEffect extends BaseStatusEffect {

    public PanicStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key, Effect.Panic);
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }
}
