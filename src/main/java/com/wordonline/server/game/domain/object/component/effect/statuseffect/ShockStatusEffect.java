package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.dto.Effect;

public class ShockStatusEffect extends StunStatusEffect {
    public ShockStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key, Effect.Shock);
    }
}
