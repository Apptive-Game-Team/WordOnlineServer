package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.effect.EffectApplyPolicy;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.StunStatusEffect;
import com.wordonline.server.game.dto.Effect;

import java.util.List;

public class ShockOverloadExplosion extends Explode {

    private static final float SHOCK_DURATION = 8f;
    private static final float FALL_SPEED_MULTIPLIER = 2f;

    public ShockOverloadExplosion(GameObject gameObject, int damage, float radius) {
        super(gameObject, damage, radius);
    }

    @Override
    protected void handleGameObject(GameObject targetObject) {
        List<Damageable> attackables = targetObject.getComponents(Damageable.class);
        if (attackables.isEmpty()) {
            return;
        }

        super.handleGameObject(targetObject);
        applyShockOverload(targetObject);
    }

    private void applyShockOverload(GameObject targetObject) {
        CommonEffectReceiver receiver = targetObject.getComponent(CommonEffectReceiver.class);
        if (receiver != null) {
            receiver.applyEffect(
                    StatusEffectKey.ShockOverload_Receive,
                    () -> new StunStatusEffect(
                            targetObject,
                            SHOCK_DURATION,
                            StatusEffectKey.ShockOverload_Receive,
                            Effect.Shock,
                            FALL_SPEED_MULTIPLIER),
                    EffectApplyPolicy.REFRESH_DURATION,
                    SHOCK_DURATION);
            return;
        }

        for (Component component : targetObject.getComponentsToAdd()) {
            if (component instanceof StunStatusEffect effect
                    && effect.getKey() == StatusEffectKey.ShockOverload_Receive) {
                effect.refresh(SHOCK_DURATION);
                return;
            }
        }
        targetObject.addComponent(new StunStatusEffect(
                targetObject,
                SHOCK_DURATION,
                StatusEffectKey.ShockOverload_Receive,
                Effect.Shock,
                FALL_SPEED_MULTIPLIER));
    }
}
