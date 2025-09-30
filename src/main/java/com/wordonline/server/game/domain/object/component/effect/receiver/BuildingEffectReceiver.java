package com.wordonline.server.game.domain.object.component.effect.receiver;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectApplyPolicy;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.*;
import com.wordonline.server.game.dto.Effect;

public class BuildingEffectReceiver extends CommonEffectReceiver {

    public BuildingEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onReceive(Effect effect) {
        switch (effect) {
            case Wet -> applyEffect(
                    StatusEffectKey.Wet_Receive,
                    () -> new WetStatusEffect(gameObject, 3f, StatusEffectKey.Wet_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
            case Burn -> applyEffect(
                    StatusEffectKey.Burn_Receive,
                    () -> new BurnStatusEffect(gameObject, 3f, StatusEffectKey.Burn_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
            case Snared -> applyEffect(
                    StatusEffectKey.Snared_Receive,
                    () -> new DOTStatusEffect(gameObject, 3f, -1, ElementType.NONE, StatusEffectKey.Snared_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
        }

    }
}
