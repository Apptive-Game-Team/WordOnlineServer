package com.wordonline.server.game.domain.object.component.effect.receiver;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.OverchargeStatusEffect;
import com.wordonline.server.game.dto.Effect;

public class LightningSummonEffectReceiver extends CommonEffectReceiver {

    private static final float OVERCHARGE_DURATION = 3f;

    public LightningSummonEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onReceive(Effect effect) {
        if (effect != Effect.Shock) {
            super.onReceive(effect);
            return;
        }

        OverchargeStatusEffect.apply(gameObject, OVERCHARGE_DURATION);
    }
}
